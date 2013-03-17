package de.friedger.android.usbnfcreader.io;

public interface Tranceiver {

	byte[] tranceive(byte[] message);
	
	void releaseDevice();
}