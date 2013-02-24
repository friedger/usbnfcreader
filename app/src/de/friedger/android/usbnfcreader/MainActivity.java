package de.friedger.android.usbnfcreader;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;
import de.friedger.android.usbnfcreader.io.NfcReaderDriver;
import de.friedger.android.usbnfcreader.io.NfcReaderListener;

public class MainActivity extends Activity implements NfcReaderListener {
	NfcReaderDriver mNfcDriver;
	private TextView mTextView;
	private Thread mDriverThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textview);

		Intent intent = getIntent();
		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
			new NfcReaderDriver(this,
					intent.getParcelableExtra(UsbManager.EXTRA_DEVICE), this);
		} else {
			new NfcReaderDriver(this, this);
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public void onConnected(NfcReaderDriver driver) {
		mNfcDriver = driver;
		mTextView.setText("connected");

		mDriverThread = new Thread(driver);
		mDriverThread.start();

	}

	public void onDisconnect() {
		mTextView.setText("disconnected");
	}

	public void onTag(final String id, long millis) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mTextView.setText("Tag " + id);
			}
		});
	}

	@Override
	public void onGetFirmwareVersion(final String ic, final int ver,
			final int rev, final int support) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				mTextView.setText("IC " + ic + " Version: " + ver + "." + rev
						+ " Support: " + support);
			}
		});

		beepTwice();
	}

	public void onButtonClick(View target) {
		if (mNfcDriver != null) {
			mNfcDriver.stop();
		}
		finish();

	}
	
	public void beepTwice(){
		Thread t = new Thread() {
			public void run() {
				MediaPlayer player = null;
				int countBeep = 0;
				while (countBeep < 2) {
					player = MediaPlayer.create(MainActivity.this, R.raw.beep);
					player.start();
					countBeep += 1;
					try {

						// 100 milisecond is duration gap between two beep
						Thread.sleep(player.getDuration() + 100);
						player.release();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}
		};

		t.start();
	}
}
