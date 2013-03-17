package de.friedger.android.usbnfcreader.io;

import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;

public class ConnectedUsbDevice {

	private UsbDeviceConnection connection;
	private UsbInterface usbInterface;
	private UsbEndpoint in;
	private UsbEndpoint out;

	public ConnectedUsbDevice(UsbDeviceConnection connection, UsbInterface usbInterface) {
		this.connection = connection;
		this.usbInterface = usbInterface;
		out = usbInterface.getEndpoint(1);
		in = usbInterface.getEndpoint(2);
	}

	public int send(byte[] buffer, int timeout) {
		return connection.bulkTransfer(out, buffer, buffer.length, timeout);
	}

	public int receive(byte[] buffer, int timeout) {
		return connection.bulkTransfer(in, buffer, buffer.length, timeout);
	}

	public void releaseDevice() {
		connection.releaseInterface(usbInterface);
		connection.close();
	}
}
