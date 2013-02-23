package de.friedger.android.usbnfcreader;

public interface NfcReaderListener {

	void onConnected(NfcReaderDriver printerDriver);
	void onDisconnect();
	
}
