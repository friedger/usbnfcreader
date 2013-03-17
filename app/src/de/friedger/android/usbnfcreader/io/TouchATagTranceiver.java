package de.friedger.android.usbnfcreader.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import de.friedger.android.usbnfcreader.Constants;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.util.Log;

public class TouchATagTranceiver implements Tranceiver {

	private UsbDeviceConnection connection;
	private UsbEndpoint in;
	private UsbEndpoint out;
	private UsbInterface usbInterface;

	public TouchATagTranceiver(UsbDeviceConnection connection, UsbInterface usbInterface) {
		this.connection = connection;
		this.usbInterface = usbInterface;
		out = usbInterface.getEndpoint(1);
		in = usbInterface.getEndpoint(2);
		cleanUpInput();
	}

	private void cleanUpInput() {
		byte buffer[] = new byte[512];
		int lengthReceived = 0;
		do {
			lengthReceived = connection.bulkTransfer(in, buffer, buffer.length, 100);
			Log.d(Constants.TAG, "Cleanup " + lengthReceived + " bytes");
		} while (lengthReceived > 0);
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

	private byte[] usbTranceive(byte[] msgToSend) throws IOException {
		Log.d(Constants.TAG, "USB-Sending: " + Utils.bufferToString(msgToSend));
		int outputResult = connection.bulkTransfer(out, msgToSend, msgToSend.length, 1000);
		if (outputResult >= 0) {
			byte[] buffer = new byte[256];
			Log.d(Constants.TAG, "Waiting for response...");
			int lengthReceived = 0;
			do {
				lengthReceived = connection.bulkTransfer(in, buffer, buffer.length, 100);
				try {
					Thread.sleep(50);
				}
				catch (InterruptedException e) {
					throw new IOException(e);
				}
			} while (lengthReceived == -1);
			byte[] result = new byte[lengthReceived];
			System.arraycopy(buffer, 0, result, 0, lengthReceived);
			Log.d(Constants.TAG, "USB-Received: " + Utils.bufferToString(result));
			return result;
		}
		else {
			throw new IOException("Timeout sending data. Error: "+outputResult);
		}
	}

	@Override
	public void releaseDevice() {
		Log.d(Constants.TAG, "releasing usb device");

		connection.releaseInterface(usbInterface);
		connection.close();
	}
}
