package de.friedger.android.usbnfcreader.io;

import java.io.IOException;

import android.util.Log;
import de.friedger.android.usbnfcreader.Constants;

public class UsbCommunication implements Runnable {

	private Tranceiver tranceiver;
	private TagListener tagListener;
	private int mIc;
	private int mVersion;
	private int mRevision;
	private int mSupport;

	public UsbCommunication(Tranceiver tranceiver) {
		this.tranceiver = tranceiver;
	}

	public void setTagListener(TagListener tagListener) {
		this.tagListener = tagListener;
	}

	private void inListPassiveTarget() throws IOException {
		byte[] response = tranceiver.tranceive(new byte[] { (byte)0xd4, 0x4a, 0x01, 0x00 });
		Log.d(Constants.TAG, "lengthIn: " + response.length);
		Log.d(Constants.TAG, Utils.bufferToString(response));
		int targets = response[2];
		if (targets >= 1) {
			int nfcidLength = response[7];
			byte[] id = new byte[nfcidLength];
			System.arraycopy(response, 8, id, 0, nfcidLength);
			Log.d(Constants.TAG, "ID: " + Utils.bufferToString(id));
			inDeselect();
			if (tagListener != null)
				tagListener.onTag(Utils.bufferToString(id));
		}
	}

	private void inAutoPoll() throws IOException {
		byte[] response = tranceiver.tranceive(new byte[] { (byte)0xd4, 0x60, 0x01, 0x01, 0x00 });
		Log.d(Constants.TAG, "lengthIn: " + response.length);
		Log.d(Constants.TAG, Utils.bufferToString(response));
		int targets = response[2];
		if (targets >= 1) {
			int targetDataOffset = 4+5; 
			Log.d("TAG", "Response: " + Utils.bufferToString(response));
			byte nfcidLength = (byte)(response[targetDataOffset] & 0xff);
			if (nfcidLength > 0) {
			byte[] id = new byte[nfcidLength];
			System.arraycopy(response, targetDataOffset+1, id, 0, nfcidLength);
			Log.d("TAG", "ID: " + Utils.bufferToString(id));
			if (tagListener != null)
				tagListener.onTag(Utils.bufferToString(id));
			}
		}
	}
	
	private void inDeselect() throws IOException {
		tranceiver.tranceive(new byte[] { (byte)0xd4, 0x44, 0x01 });
		// Log.d(TAG, "lengthIn: " + response.length);
		// Log.d(TAG, Utils.bufferToString(response));
	}

	private void getFirmwareVersion() throws IOException {
		byte[] response = tranceiver.tranceive(new byte[] { (byte)0xd4, 0x02 });
		Log.d(Constants.TAG, "lengthIn: " + response.length);
		Log.d(Constants.TAG, Utils.bufferToString(response));
		mIc = response[2];
		mVersion = response[3];
		mRevision = response[4];
		mSupport = response[5];
		Log.d(Constants.TAG, "IC " + Integer.toHexString(mIc) + " Version: " + mVersion + "." + mRevision + " Support: " + mSupport);
	}

	@Override
	public void run() {
		try {
			getFirmwareVersion();
			while (!Thread.interrupted()) {
				try {
//					inListPassiveTarget();
						inAutoPoll();
				}
				catch (IOException e) {
					if (e.getCause() != null && e.getCause() instanceof InterruptedException) {
						Log.d(Constants.TAG, "usb thread interrupted");
					}
					else {
						e.printStackTrace();
						if (tagListener != null)
							tagListener.onError(e.getMessage());
					}
					break;
				}
				try {
					Thread.sleep(10);
				}
				catch (InterruptedException e) {
					Log.d(Constants.TAG, "usb thread interrupted");
					break;
				}
			}
		}
		catch (IOException e) {
			if (tagListener != null)
				tagListener.onError(e.getMessage());
			e.printStackTrace();
		}
		tranceiver.releaseDevice();
		Log.d(Constants.TAG, "usb device released");
	}
}
