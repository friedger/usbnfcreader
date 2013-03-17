package de.friedger.android.usbnfcreader.io;

import java.io.IOException;

public interface Tranceiver {

	byte[] tranceive(byte[] message) throws IOException;

	void releaseDevice();
}