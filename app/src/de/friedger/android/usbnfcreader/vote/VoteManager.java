package de.friedger.android.usbnfcreader.vote;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import de.friedger.android.usbnfcreader.Constants;
import de.friedger.android.usbnfcreader.io.UsbCommunication;

public class VoteManager {

	private Map<VoteType, Thread> votingMaschines = new HashMap<VoteType, Thread>();
	private String roomId;
	private VoteListener voteListener;

	public VoteManager(String roomId, VoteListener voteListener) {
		this.roomId = roomId;
		this.voteListener = voteListener;
	}

	public void addNfcReader(UsbCommunication usbCommunication) {
		for (VoteType voteType : VoteType.values()) {
			if (!votingMaschines.containsKey(voteType)) {
				usbCommunication.setTagListener(new VotingMachine(this, voteType));
				Thread thread = new Thread(usbCommunication);
				votingMaschines.put(voteType, thread);
				thread.start();
				Log.d(Constants.TAG, "Registered " + usbCommunication + " for vote " + voteType);
				return;
			}
		}
	}

	public void stop() {
		for (Thread thread : votingMaschines.values()) {
			thread.interrupt();
		}
	}

	public void submitVote(VoteType vote, String id) {
		Log.d(Constants.TAG, "new vote: " + vote + " with ID: " + id);
		if (voteListener != null) {
			voteListener.onVote(new Vote(vote, id, roomId, System.currentTimeMillis()));
		}
	}
}
