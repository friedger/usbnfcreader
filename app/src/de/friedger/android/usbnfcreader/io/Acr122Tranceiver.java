package de.friedger.android.usbnfcreader.io;

import java.io.IOException;

public class Acr122Tranceiver extends AbstractTranceiver {

	public Acr122Tranceiver(ConnectedUsbDevice connectedUsbDevice) {
		super(connectedUsbDevice, (byte)0x6b);
		powerOn();
		getFirmware();
	}

	@Override
	public byte[] tranceive(byte[] message) throws IOException {
		byte[] response = usbTranceive(createMessage(0, 0, 0, message));
		if (response[response.length - 2] == (byte)0x90 && response[response.length - 1] == 0x00) {
			byte[] result = new byte[response.length - 2];
			System.arraycopy(response, 10, result, 0, response.length - 2 - 10);
			return result;
		}
		else
			throw new IOException("Error getting response [" + Utils.bufferToString(response) + "]");
	}
}
