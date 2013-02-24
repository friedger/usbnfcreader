package de.friedger.android.usbnfcreader;

import java.nio.ByteBuffer;
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
import android.hardware.usb.UsbRequest;
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

	public String readTag() {
		Log.d(TAG, "reading");
		byte[] buffer = new byte[265];
		mConnection.bulkTransfer(mIn, buffer, 265, 0);

		String result = bufferToString(buffer);
		Log.d(TAG, result);
		byte type = buffer[0];
		return result;
	}

    final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	private String bufferToString(byte[] buffer) {
		return bufferToString(buffer, buffer.length);
	}
	
	private String bufferToString(byte[] buffer, int length) {

		length = Math.max(length, 0);
		
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			byte b = buffer[i];
			sb.append(String.valueOf(b));
		}
		sb.append("\n");
		
		    char[] hexChars = new char[length * 2];
		    int v;
		    for ( int j = 0; j < length; j++ ) {
		        v = buffer[j] & 0xFF;
		        hexChars[j * 2] = hexArray[v >>> 4];
		        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		    }
		    sb.append(hexChars);		    
		
		return sb.toString();
	}

	
	private void sendGetFirmewareVersion() {
		synchronized (this) {
			if (mConnection != null) {
				TouchATagTranceiver tranceiver = new TouchATagTranceiver(mConnection, mIn, mOut);
				byte[] response = tranceiver.tranceive(new byte[] {(byte)0xd4, 0x02});
				Log.d(TAG, "lengthIn: " + response.length);
				Log.d(TAG, Utils.bufferToString(response));
				
				int ic = response[2];
				int ver = response[3];
				int rev = response[4];
				int support = response[5];

				Log.d(TAG, "IC "+Integer.toHexString(ic)+" Version: "+ver+"."+rev+" Support: "+support);
			}
		}
	}

	@Override
	public void run() {
		ByteBuffer buffer = ByteBuffer.allocate(265); 		
		UsbRequest request = new UsbRequest();
		request.initialize(mConnection, mInterrupt);
		while (true) {
			// queue a request on the interrupt endpoint
			//request.queue(buffer, 265);

			sendGetFirmewareVersion();

			// wait for status event
			if (mConnection.requestWait() == request) {
				Log.d(TAG, "got response ");
				Log.d(TAG, bufferToString(buffer.array()));
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			} else {
				Log.e(TAG, "requestWait failed, exiting");
				break;
			}
		}
	}

}
