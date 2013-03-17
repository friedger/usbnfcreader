package de.friedger.android.usbnfcreader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import de.friedger.android.usbnfcreader.io.AttachedDeviceHandler;
import de.friedger.android.usbnfcreader.io.UsbConnectionException;
import de.friedger.android.usbnfcreader.vote.Vote;
import de.friedger.android.usbnfcreader.vote.VoteListener;
import de.friedger.android.usbnfcreader.vote.VoteManager;

public class MainActivity extends Activity implements VoteListener {
	
	private VoteManager voteManager;
	private TextView mTextView;
	private Beeper beeper;
	private AttachedDeviceHandler attachedDeviceHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
		voteManager = new VoteManager("room1", this);
	}
	
	@Override
	public void onVote(final Vote vote) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextView.setText("Vote: "+vote.getVoteType()+" Id: "+vote.getId()+" Room: "+vote.getRoomId());
				beepTwice();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	public void onButtonClick(View target) {
		finish();
	}
	
	@Override
	protected void onStop() {
		voteManager.stop();
		attachedDeviceHandler.onStop();
		super.onStop();
	}
	
	public void beepTwice(){
		Thread t = new Thread(beeper);
		t.start();
	}
}
