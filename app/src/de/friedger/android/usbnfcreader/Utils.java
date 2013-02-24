package de.friedger.android.usbnfcreader;

public class Utils {

	private static final char[] hexArray = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E',
			'F' };

	public static String bufferToString(byte[] buffer) {
		return bufferToString(buffer, buffer.length);
	}

	public static String bufferToString(byte[] buffer, int length) {
		StringBuffer sb = new StringBuffer();
		char[] hexChars = new char[length * 2];
		int v;
		for (int j = 0; j < length; j++) {
			v = buffer[j] & 0xFF;
			hexChars[j * 2] = hexArray[v >>> 4];
			hexChars[j * 2 + 1] = hexArray[v & 0x0F];
		}
		sb.append(hexChars);
		return sb.toString();
	}
}
