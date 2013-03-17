package de.friedger.android.usbnfcreader.io;

import android.util.Log;
import de.friedger.android.usbnfcreader.Constants;

public class UsbCommunication implements Runnable {

	private Tranceiver tranceiver;
	private TagListener tagListener;
	private NfcReaderListener nfcReaderListener;

	public UsbCommunication(Tranceiver tranceiver) {
		this.tranceiver = tranceiver;
	}

	public void setTagListener(TagListener tagListener) {
		this.tagListener = tagListener;
	}

	private void inListPassiveTarget() {
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

	private void inDeselect() {
		tranceiver.tranceive(new byte[] { (byte)0xd4, 0x44, 0x01 });
		// Log.d(TAG, "lengthIn: " + response.length);
		// Log.d(TAG, Utils.bufferToString(response));
	}

	private void getFirmwareVersion() {
		byte[] response = tranceiver.tranceive(new byte[] { (byte)0xd4, 0x02 });
		Log.d(Constants.TAG, "lengthIn: " + response.length);
		Log.d(Constants.TAG, Utils.bufferToString(response));
		int ic = response[2];
		int ver = response[3];
		int rev = response[4];
		int support = response[5];
		Log.d(Constants.TAG, "IC " + Integer.toHexString(ic) + " Version: " + ver + "." + rev + " Support: " + support);
		if (nfcReaderListener != null)
			nfcReaderListener.onGetFirmwareVersion(Integer.toHexString(ic), ver, rev, support);
	}

	@Override
	public void run() {
		while (!Thread.interrupted()) {
			inListPassiveTarget();
			try {
				Thread.sleep(100);
			}
			catch (InterruptedException e) {
				Log.d(Constants.TAG, "usb thread interrupted");
			}
		}
		tranceiver.releaseDevice();
		Log.d(Constants.TAG, "usb device released");
	}
}
