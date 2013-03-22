package de.friedger.android.usbnfcreader.io;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import de.friedger.android.usbnfcreader.Constants;
import de.friedger.android.usbnfcreader.vote.VoteManager;

public class AttachedDeviceHandler {

	private Context context;
	private VoteManager voteManager;
	private UsbManager usbManager;
	private Set<PermissionBroadcastReceiver> broadcastReceivers = new HashSet<PermissionBroadcastReceiver>();

	public AttachedDeviceHandler(Context context, VoteManager voteManager) {
		this.context = context;
		this.voteManager = voteManager;
		usbManager = (UsbManager)context.getSystemService(Context.USB_SERVICE);
	}

	public void onStop() {
		for (PermissionBroadcastReceiver br : broadcastReceivers) {
			context.unregisterReceiver(br);
		}
	}

	public void handleIntent(Intent intent) throws UsbConnectionException {
		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(intent.getAction())) {
			UsbDevice usbDevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
			connectWithPermission(usbDevice);
		}
		else {
			initConnectedDevices();
		}
	}

	public void initConnectedDevices() throws UsbConnectionException {
		Map<String, UsbDevice> deviceList = usbManager.getDeviceList();
		boolean found = false;
		for (Map.Entry<String, UsbDevice> e : deviceList.entrySet()) {
			Log.d(Constants.TAG, e.getKey() + " " + e.getValue());
			UsbDevice device = e.getValue();
			int vendorId = device.getVendorId();
			int productId = device.getProductId();
			Log.d(Constants.TAG, Integer.toHexString(vendorId) + " " +  Integer.toHexString(productId) );
			if (vendorId == 0x072f && (productId == 0x2200 || productId == 0x90cc)) {
				connectWithPermission(device);
				found = true;
			}
		}
		if (!found) {
			Log.e(Constants.TAG, "device not found");
		}
	}

	private void connectWithPermission(UsbDevice device) throws UsbConnectionException {
		if (!usbManager.hasPermission(device)) {
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(Constants.ACTION_USB_PERMISSION), 0);
			IntentFilter filter = new IntentFilter(Constants.ACTION_USB_PERMISSION);
			PermissionBroadcastReceiver permissionBroadcastReceiver = new PermissionBroadcastReceiver(this);
			broadcastReceivers.add(permissionBroadcastReceiver);
			context.registerReceiver(permissionBroadcastReceiver, filter);
			usbManager.requestPermission(device, pi);
		}
		else {
			connectToNfcReader(device);
		}
	}

	protected void connectToNfcReader(UsbDevice usbDevice) throws UsbConnectionException {
		Log.d(Constants.TAG, "has permission: " + usbManager.hasPermission(usbDevice));
		UsbDeviceConnection connection = usbManager.openDevice(usbDevice);
		if (connection != null) {
			UsbInterface usbInterface = usbDevice.getInterface(0);
			if (!connection.claimInterface(usbInterface, true)) {
				throw new UsbConnectionException("no exclusive rights");
			}
			ConnectedUsbDevice connectedUsbDevice = new ConnectedUsbDevice(connection, usbInterface);
			byte[] rawDescriptors = connection.getRawDescriptors();
			byte deviceId = rawDescriptors[65];
			Log.d(Constants.TAG, "device: " + Integer.toHexString(deviceId));
			Tranceiver tranceiver = null;
			if ( deviceId == 0) {
				tranceiver = new TouchATagTranceiver(connectedUsbDevice);				
			}
			else if (deviceId == 1) {
				tranceiver = new Acr122Tranceiver(connectedUsbDevice);
			}
			if (tranceiver != null) {
				UsbCommunication usbCommunication = new UsbCommunication(tranceiver);
				voteManager.addNfcReader(usbCommunication);
				Log.d(Constants.TAG, "connected");
			}
			else
				Log.d(Constants.TAG, "NO handler found for deviceID "+deviceId);
				
		}
		else {
			throw new UsbConnectionException("null connection received");
		}
	}
}
