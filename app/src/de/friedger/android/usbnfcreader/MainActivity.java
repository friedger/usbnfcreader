package de.friedger.android.usbnfcreader;

import android.app.Activity;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity implements NfcReaderListener {
	NfcReaderDriver mNfcDriver;
	private TextView mTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mTextView = (TextView) findViewById(R.id.textview);
		
		
		Intent intent = getIntent();
		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())){
			new NfcReaderDriver(this, intent.getParcelableExtra(UsbManager.EXTRA_DEVICE), this);
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
		mNfcDriver= driver;
		mTextView.setText("connected");
		
        Thread thread = new Thread(driver);
        thread.start();

	}
	
	public void onDisconnect(){
		mTextView.setText("disconnected");
	}
	
	
	@Override
	public void onGetFirmwareVersion(final String ic, final int ver, final int rev, final int support) {
		runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				mTextView.setText("IC "+ic+" Version: "+ver+"."+rev+" Support: "+support);		
			}
		});
	}
	
	public void onButtonClick(View target){
		if (mNfcDriver != null){
			String result = mNfcDriver.readTag();
		}

	}
}
