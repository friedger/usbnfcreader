package de.friedger.android.usbnfcreader.io;

import java.util.Map;

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

public class NfcReaderDriver implements Runnable {
	private UsbManager mUsb;
	private UsbDevice mNfcReader;

	private final String TAG = "UsbNfcReader";

	private static final String ACTION_USB_PERMISSION = "de.friedger.android.usbnfcreader.USB_PERMISSION";

	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
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
						Log.d(TAG, "permission denied for device " + device);
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
			Log.d(TAG, e.getKey() + " " + e.getValue());
			UsbDevice device = e.getValue();
			int vendorId = device.getVendorId();
			int productId = device.getProductId();

			if (vendorId == 0x072f && productId == 0x2200) {
				mNfcReader = device;
				if (!mUsb.hasPermission(mNfcReader)) {
					PendingIntent pi = PendingIntent.getBroadcast(context, 0,
							new Intent(ACTION_USB_PERMISSION), 0);
					IntentFilter filter = new IntentFilter(
							ACTION_USB_PERMISSION);
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
			Log.e(TAG, "device not found");
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
			Log.d(TAG, "error");
			return;
		}

		mInterrupt = i.getEndpoint(0);
		if (mInterrupt.getType() != UsbConstants.USB_ENDPOINT_XFER_INT) {
			Log.e(TAG, "endpoint is not interrupt type");
			return;
		}

		mOut = i.getEndpoint(1);
		Log.d(TAG, "" + mOut);
		Log.d(TAG, "out? " + (mOut.getDirection() == UsbConstants.USB_DIR_OUT));

		mIn = i.getEndpoint(2);
		Log.d(TAG, "" + mIn);
		Log.d(TAG, "in? " + (mIn.getDirection() == UsbConstants.USB_DIR_IN));

		Log.d(TAG, "connected");
		if (mListener != null) {
			mListener.onConnected(this);
		}

	}

	private void inListPassiveTarget() {
		TouchATagTranceiver tranceiver = new TouchATagTranceiver(mConnection,
				mIn, mOut);
		byte[] response = tranceiver.tranceive(new byte[] { (byte) 0xd4, 0x4a,
				0x01, 0x00 });
		Log.d(TAG, "lengthIn: " + response.length);
		Log.d(TAG, Utils.bufferToString(response));
		int targets = response[2];
		if (targets >= 1) {
			int nfcidLength = response[7];
			byte[] id = new byte[nfcidLength];
			System.arraycopy(response, 8, id, 0, nfcidLength);
			Log.d(TAG, "ID: " + Utils.bufferToString(id));
			inDeselect();
			mListener.onTag(Utils.bufferToString(id), System.currentTimeMillis());
		}
	}

	private void inDeselect() {
		TouchATagTranceiver tranceiver = new TouchATagTranceiver(mConnection,
				mIn, mOut);
		byte[] response = tranceiver.tranceive(new byte[] { (byte) 0xd4, 0x44,
				0x01 });
		// Log.d(TAG, "lengthIn: " + response.length);
		// Log.d(TAG, Utils.bufferToString(response));
	}

	private void getFirmwareVersion() {
		if (mConnection != null) {
			TouchATagTranceiver tranceiver = new TouchATagTranceiver(
					mConnection, mIn, mOut);
			byte[] response = tranceiver.tranceive(new byte[] { (byte) 0xd4,
					0x02 });
			Log.d(TAG, "lengthIn: " + response.length);
			Log.d(TAG, Utils.bufferToString(response));

			int ic = response[2];
			int ver = response[3];
			int rev = response[4];
			int support = response[5];

			Log.d(TAG, "IC " + Integer.toHexString(ic) + " Version: " + ver
					+ "." + rev + " Support: " + support);
			mListener.onGetFirmwareVersion(Integer.toHexString(ic), ver, rev,
					support);
		}
	}

	@Override
	public void run() {

		getFirmwareVersion();

		mStopped = false;
		
		while (!mStopped) {

			inListPassiveTarget();

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
	}

	public void stop() {
		mStopped = true;
	}
}
