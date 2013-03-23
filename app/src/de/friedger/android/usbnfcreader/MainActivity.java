package de.friedger.android.usbnfcreader;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import de.friedger.android.usbnfcreader.io.AttachedDeviceHandler;
import de.friedger.android.usbnfcreader.io.UsbConnectionException;
import de.friedger.android.usbnfcreader.vote.Vote;
import de.friedger.android.usbnfcreader.vote.VoteDbHelper;
import de.friedger.android.usbnfcreader.vote.VoteListener;
import de.friedger.android.usbnfcreader.vote.VoteManager;

public class MainActivity extends Activity implements VoteListener {
	
	private VoteManager voteManager;
	private TextView mTextView;
	private Beeper beeper;
	private AttachedDeviceHandler attachedDeviceHandler;
	private VoteDbHelper mDbHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mDbHelper = new VoteDbHelper(this);
		
		initUi();
		initVoteManager();

		Intent intent = getIntent();
		attachedDeviceHandler = new AttachedDeviceHandler(this, voteManager);
		
		try {
			attachedDeviceHandler.handleIntent(intent);
			mTextView.setText("OK");
		}
		catch (UsbConnectionException e) {
			mTextView.setText(e.getMessage());
		}
	}


	private void initUi() {
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textview);
		beeper = new Beeper(this);
	}
	
	private void initVoteManager() {
		voteManager = new VoteManager("undefined", this);
	}
	
	@Override
	public void onVote(final Vote vote) {		
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextView.setText("Vote: "+vote.getVoteType()+" Id: "+vote.getId()+" Room: "+vote.getRoomId());
			}
		});
		//blink(vote.getVoteType());
		storeVote(vote);
	}
	
	public void storeVote(Vote vote){
		SQLiteDatabase db = mDbHelper.getWritableDatabase();
		
		ContentValues values = new ContentValues();
		values.put(Vote.COLUMN_NAME_ROOM_ID, vote.getRoomId());
		values.put(Vote.COLUMN_NAME_VOTE_TYPE, vote.getVoteType().name());
		values.put(Vote.COLUMN_NAME_ID, vote.getId());
		values.put(Vote.COLUMN_NAME_TIMESTAMP, vote.getTimestamp());
		
		db.insert(VoteDbHelper.TABLE_NAME, "null", values);
		
	}

	public void onButtonClick(View target) {
		finish();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		attachedDeviceHandler.onStop();
		voteManager.onStop();
	}
	
	public void beepTwice(){
		Thread t = new Thread(beeper);
		t.start();
	}
}
