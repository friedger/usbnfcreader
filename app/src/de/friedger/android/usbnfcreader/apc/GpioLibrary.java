package de.friedger.android.usbnfcreader.apc;

import android.util.Log;

public class GpioLibrary {

	
	 private static final String TAG = "GpioLibrary";

	static {
		 try{
		    System.loadLibrary("wmtgpio");
		 } catch (Exception e){
			 Log.v(TAG, e.toString());
		 }
	 }
		  
}

