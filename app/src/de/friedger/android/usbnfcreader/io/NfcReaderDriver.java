package de.friedger.android.usbnfcreader.io;

import java.util.Map;

import de.friedger.android.usbnfcreader.Constants;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Parcelable;
import android.util.Log;
import android.widget.Toast;

public class NfcReaderDriver  {
	private UsbManager mUsb;
	private UsbDevice mNfcReader;

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (Constants.ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent
							.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(
							UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (device != null) {
							mNfcReader = device;
							connectToNfcReader();
						}
					} else {
						Log.d(Constants.TAG, "permission denied for device " + device);
					}
				}
			} else {
				Toast.makeText(context, action, Toast.LENGTH_LONG).show();
			}
		}
	};
	private NfcReaderListener mListener;
	private UsbEndpoint mIn;
	private UsbDeviceConnection mConnection;
	private UsbEndpoint mInterrupt;
	private UsbEndpoint mOut;
	private boolean mStopped;

	public NfcReaderDriver(Context context, NfcReaderListener listener) {
		mUsb = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		mListener = listener;

		Map<String, UsbDevice> deviceList = mUsb.getDeviceList();

		boolean found = false;
		for (Map.Entry<String, UsbDevice> e : deviceList.entrySet()) {
			Log.d(Constants.TAG, e.getKey() + " " + e.getValue());
			UsbDevice device = e.getValue();
			int vendorId = device.getVendorId();
			int productId = device.getProductId();


			if (vendorId == 0x072f && productId == 0x2200) {
				mNfcReader = device;
				if (!mUsb.hasPermission(mNfcReader)) {
					PendingIntent pi = PendingIntent.getBroadcast(context, 0,
							new Intent(Constants.ACTION_USB_PERMISSION), 0);
					IntentFilter filter = new IntentFilter(
							Constants.ACTION_USB_PERMISSION);
					context.registerReceiver(mUsbReceiver, filter);
					mUsb.requestPermission(mNfcReader, pi);

				} else {

					connectToNfcReader();
				}
				found = true;
				break;
			}

		}

		if (!found) {
			Log.e(Constants.TAG, "device not found");
		}

	}

	public NfcReaderDriver(Context context, Parcelable usbDevice,
			NfcReaderListener listener) {
		mUsb = (UsbManager) context.getSystemService(Context.USB_SERVICE);
		mNfcReader = (UsbDevice) usbDevice;
		mListener = listener;
	}

	private void connectToNfcReader() {
		mConnection = mUsb.openDevice(mNfcReader);
		UsbInterface i = mNfcReader.getInterface(0);
		
		if (!mConnection.claimInterface(i, true)) {
			Log.d(Constants.TAG, "error");
			return;
		}

		mInterrupt = i.getEndpoint(0);
		if (mInterrupt.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
			Log.e(Constants.TAG, "endpoint is not interrupt type");
			return;
		}

		mOut = i.getEndpoint(1);
		Log.d(Constants.TAG, "" + mOut);
		Log.d(Constants.TAG, "out? " + (mOut.getDirection() == UsbConstants.USB_DIR_OUT));

		mIn = i.getEndpoint(2);
		Log.d(Constants.TAG, "" + mIn);
		Log.d(Constants.TAG, "in? " + (mIn.getDirection() == UsbConstants.USB_DIR_IN));

		Log.d(Constants.TAG, "connected");
//		if (mListener != null) {
//			mListener.onConnected(this);
//		}

	}

}
