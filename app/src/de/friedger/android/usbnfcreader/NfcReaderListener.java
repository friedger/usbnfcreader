package de.friedger.android.usbnfcreader;

public interface NfcReaderListener {

	void onConnected(NfcReaderDriver printerDriver);
	void onDisconnect();
	void onGetFirmwareVersion(String ic, int ver, int rev, int support);
	
}
