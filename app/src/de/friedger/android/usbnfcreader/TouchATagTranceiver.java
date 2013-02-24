package de.friedger.android.usbnfcreader;

import java.nio.ByteBuffer;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.util.Log;

public class TouchATagTranceiver {

	private final String TAG = "UsbNfcReader";
	private UsbDeviceConnection connection;
	private UsbEndpoint in;
	private UsbEndpoint out;

	public TouchATagTranceiver(UsbDeviceConnection connection, UsbEndpoint in, UsbEndpoint out) {
		this.connection = connection;
		this.in = in;
		this.out = out;
	}

	public byte[] tranceive(byte[] message) {
		int exptectedResponseSize = sendRequest(message);
		return getResponse(exptectedResponseSize);
	}

	private byte[] getResponse(int expectedResponseSize) {
		byte[] touchatagmsgPrefix = { 0x6f, 0x05, 0x00, 0x00, 0x00, 0x00, 0x0f, 0x00, 0x00, 0x00, (byte)0xff,
				(byte)0xc0, 0x00, 0x00, 0x00 };
		touchatagmsgPrefix[touchatagmsgPrefix.length - 1] = (byte)expectedResponseSize;
		byte[] response = usbTranceive(touchatagmsgPrefix);
		// 8008000000000f000000 xyz 9000
		if (response[response.length - 2] == (byte)0x90 && response[response.length - 1] == 0x00) {
			byte[] result = new byte[expectedResponseSize-2];
			System.arraycopy(response, 10, result, 0, expectedResponseSize-2);
			return result;
		}
		else
			throw new RuntimeException("Error getting response [" + Utils.bufferToString(response) + "]");
	}

	private int sendRequest(byte[] message) {
		byte[] touchatagmsgPrefix = { 0x6f, 0x07, 0x00, 0x00, 0x00, 0x00, 0x0e, 0x00, 0x00, 0x00, (byte)0xff, 0x00,
				0x00, 0x00 };
		ByteBuffer msgToSend = ByteBuffer.allocate(touchatagmsgPrefix.length + 1 + message.length);
		msgToSend.put(touchatagmsgPrefix);
		msgToSend.put((byte)message.length);
		msgToSend.put(message);
		byte[] response = usbTranceive(msgToSend.array());
		if (response.length == 12) {
			return response[11];
		}
		else
			throw new RuntimeException("Cannot parse response [" + Utils.bufferToString(response) + "]");
	}

	private byte[] usbTranceive(byte[] msgToSend) {
		Log.d(TAG, "USB-Sending: "+Utils.bufferToString(msgToSend));
		connection.bulkTransfer(out, msgToSend, msgToSend.length, 0);
		byte[] buffer = new byte[256];
		int lengthReceived = connection.bulkTransfer(in, buffer, buffer.length, 0);
		byte[] result = new byte[lengthReceived];
		System.arraycopy(buffer, 0, result, 0, lengthReceived);
		Log.d(TAG, "USB-Received: "+Utils.bufferToString(result));
		return result;
	}
}
