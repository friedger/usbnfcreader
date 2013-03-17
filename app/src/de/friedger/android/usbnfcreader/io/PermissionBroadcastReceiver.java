package de.friedger.android.usbnfcreader.io;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import android.widget.Toast;
import de.friedger.android.usbnfcreader.Constants;

public class PermissionBroadcastReceiver extends BroadcastReceiver {

	private AttachedDeviceHandler attachedDeviceHandler;

	public PermissionBroadcastReceiver(AttachedDeviceHandler attachedDeviceHandler) {
		this.attachedDeviceHandler = attachedDeviceHandler;
	}

	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Constants.ACTION_USB_PERMISSION.equals(action)) {
			UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
				if (device != null) {
					try {
						attachedDeviceHandler.connectToNfcReader(device);
					}
					catch (UsbConnectionException e) {
						e.printStackTrace();
					}
				}
			}
			else {
				Log.d(Constants.TAG, "permission denied for device " + device);
			}
		}
		else {
			Toast.makeText(context, action, Toast.LENGTH_LONG).show();
		}
	}
}
