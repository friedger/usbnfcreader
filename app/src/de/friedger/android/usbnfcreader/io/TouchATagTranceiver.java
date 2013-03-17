package de.friedger.android.usbnfcreader.io;

import java.io.IOException;
import java.nio.ByteBuffer;

public class TouchATagTranceiver extends AbstractTranceiver {

	public TouchATagTranceiver(ConnectedUsbDevice connectedUsbDevice) {
		super(connectedUsbDevice, (byte)0x6f);
		powerOn();
		getFirmware();
	}

	@Override
	public byte[] tranceive(byte[] message) throws IOException {
		int exptectedResponseSize = sendRequest(message);
		return getResponse(exptectedResponseSize);
	}

	private byte[] getResponse(int expectedResponseSize) throws IOException {
		byte[] touchatagmsgPrefix = { 0x6f, 0x05, 0x00, 0x00, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, (byte)0xff,
				(byte)0xc0, 0x00, 0x00, 0x00 };
		touchatagmsgPrefix[touchatagmsgPrefix.length - 1] = (byte)expectedResponseSize;
		byte[] response = usbTranceive(touchatagmsgPrefix);
		// 8008000000000f000000 xyz 9000
		if (response[response.length - 2] == (byte)0x90 && response[response.length - 1] == 0x00) {
			byte[] result = new byte[expectedResponseSize - 2];
			System.arraycopy(response, 10, result, 0, expectedResponseSize - 2);
			return result;
		}
		else
			throw new IOException("Error getting response [" + Utils.bufferToString(response) + "]");
	}

	private int sendRequest(byte[] message) throws IOException {
		byte[] touchatagmsgPrefix = { 0x6f, 0x07, 0x00, 0x00, 0x00, 0x00, 0x0e, 0x00, 0x00, 0x00, (byte)0xff, 0x00,
				0x00, 0x00 };
		touchatagmsgPrefix[1] = (byte)(4 + 1 + message.length);
		ByteBuffer msgToSend = ByteBuffer.allocate(touchatagmsgPrefix.length + 1 + message.length);
		msgToSend.put(touchatagmsgPrefix);
		msgToSend.put((byte)message.length);
		msgToSend.put(message);
		byte[] response = usbTranceive(msgToSend.array());
		if (response.length == 12) {
			return response[11];
		}
		else
			throw new IOException("Cannot parse response [" + Utils.bufferToString(response) + "]");
	}

}
