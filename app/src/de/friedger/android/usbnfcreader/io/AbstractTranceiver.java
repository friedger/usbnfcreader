package de.friedger.android.usbnfcreader.io;

import java.io.IOException;
import java.nio.ByteBuffer;

import android.util.Log;
import de.friedger.android.usbnfcreader.Constants;

public abstract class AbstractTranceiver implements Tranceiver {

	private ConnectedUsbDevice connectedUsbDevice;
	private byte prefixByte;

	protected AbstractTranceiver(ConnectedUsbDevice connectedUsbDevice, byte prefixByte) {
		this.connectedUsbDevice = connectedUsbDevice;
		this.prefixByte = prefixByte;
	}

	protected void cleanUpInput() {
		byte buffer[] = new byte[512];
		int lengthReceived = 0;
		do {
			lengthReceived = connectedUsbDevice.receive(buffer, 100);
			String result = "";
			if (lengthReceived > 0)
				result = Utils.bufferToString(buffer, lengthReceived);
			Log.d(Constants.TAG, "Cleanup " + lengthReceived + " bytes, " + result);
		} while (lengthReceived > 0);
	}

	protected void powerOn() {
		byte[] powerOnCmd = { 0x62, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00 };
		int outputResult = connectedUsbDevice.send(powerOnCmd, 1000);
		Log.d(Constants.TAG, "power on");
		cleanUpInput();
	}

	protected void getFirmware() {
		byte[] msg = createMessage(0, 0x48, 0, new byte[0]);
		int outputResult = connectedUsbDevice.send(msg, 1000);
		Log.d(Constants.TAG, "getfirmware");
		cleanUpInput();
	}

	protected byte[] createMessage(int ins, int p1, int p2, byte[] message) {
		byte[] prefix = { prefixByte, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0a, 0x00, 0x00, 0x00, (byte)0xff, (byte)ins,
				(byte)p1, (byte)p2 };
		prefix[1] = (byte)(4 + 1 + message.length);
		ByteBuffer msgToSend = ByteBuffer.allocate(prefix.length + 1 + message.length);
		msgToSend.put(prefix);
		msgToSend.put((byte)message.length);
		msgToSend.put(message);
		return msgToSend.array();
	}

	protected byte[] usbTranceive(byte[] msgToSend) throws IOException {
		Log.d(Constants.TAG, "USB-Sending: " + Utils.bufferToString(msgToSend));
		int outputResult = connectedUsbDevice.send(msgToSend, -1);
		if (outputResult >= 0) {
			byte[] buffer = new byte[256];
			Log.d(Constants.TAG, "Waiting for response...");
			int lengthReceived = 0;
			do {
				lengthReceived = connectedUsbDevice.receive(buffer, 100);
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
			throw new IOException("Timeout sending data. Error: " + outputResult);
		}
	}

	@Override
	public void releaseDevice() {
		Log.d(Constants.TAG, "releasing usb device");
		connectedUsbDevice.releaseDevice();
	}
}
