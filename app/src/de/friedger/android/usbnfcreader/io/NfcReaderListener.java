package de.friedger.android.usbnfcreader.io;

public interface NfcReaderListener {

	void onConnected(NfcReaderDriver printerDriver);
	void onDisconnect();
	void onGetFirmwareVersion(String ic, int ver, int rev, int support);
	void onTag(String tagId, long currentTimeMillis);
	
}
