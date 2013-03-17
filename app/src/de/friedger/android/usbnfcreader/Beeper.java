package de.friedger.android.usbnfcreader;

import android.content.Context;
import android.media.MediaPlayer;

public class Beeper implements Runnable {

	private Context context;

	public Beeper(Context context) {
		this.context = context;
	}

	@Override
	public void run() {
		MediaPlayer player = null;
		int countBeep = 0;
		while (countBeep < 2) {
			player = MediaPlayer.create(context, R.raw.beep);
			player.start();
			countBeep += 1;
			try {
				// 100 milisecond is duration gap between two beep
				Thread.sleep(player.getDuration() + 100);
				player.release();
			}
			catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
