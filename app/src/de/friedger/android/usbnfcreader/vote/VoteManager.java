package de.friedger.android.usbnfcreader.vote;

import java.util.HashMap;
import java.util.Map;

import android.util.Log;
import de.friedger.android.usbnfcreader.io.UsbCommunication;

public class VoteManager {

	private static final String LOG_TAG = "VoteManager";
	
	private static final int STATUS_NORMAL = 0;
	private static final int STATUS_CONFIGURE = 1;
	
	private Map<VotingMachine, Thread> communicationThreads = new HashMap<VotingMachine, Thread>();
	private Map<VoteType, VotingMachine> votingMachines = new HashMap<VoteType, VotingMachine>();
	
	private String roomId;
	private VoteListener voteListener;
	
	private int mStatus = STATUS_NORMAL;

	public VoteManager(String roomId, VoteListener voteListener) {
		this.roomId = roomId;
		this.voteListener = voteListener;
	}

	public void addNfcReader(UsbCommunication usbCommunication) {

		VotingMachine votingMachine = new VotingMachine(this);
		usbCommunication.setTagListener(votingMachine);
		
		Thread thread = new Thread(usbCommunication);
		communicationThreads.put(votingMachine, thread);
		thread.start();
		Log.d(LOG_TAG, "Registered " + usbCommunication);
		return;
	}

	public void onStop() {
		for (Thread thread : communicationThreads.values()) {
			thread.interrupt();
		}
		communicationThreads.clear();
	}

	public void configureMachine(VotingMachine votingMachine, String id){
		Log.d(LOG_TAG, "Configure by ID:" + id);

		VoteType voteType = MasterTags.identifyVoteTypeById(id);
		
		if (!votingMachines.containsKey(voteType)){
			votingMachine.setVoteType(voteType);
			votingMachines.put(voteType, votingMachine);
		}
	}

	public void resetMachines(){
		for (VotingMachine v : votingMachines.values()){
			v.setVoteType(VoteType.UNDEFINED);
		}
		votingMachines.clear();
	}
	
	public void submitVote(VoteType vote, String id) {		
		
		
		if (mStatus == STATUS_CONFIGURE){
			Log.d(LOG_TAG, "new room: " + id);
			roomId = id;
			mStatus = STATUS_NORMAL;
		} else {
			Log.d(LOG_TAG, "new vote: " + vote + " with ID: " + id);	
		}

		VoteType voteType = MasterTags.identifyVoteTypeById(id);
		if (VoteType.UNDEFINED == voteType){
			mStatus = STATUS_CONFIGURE;
		}		
		
		if (voteListener != null) {
			voteListener.onVote(new Vote(vote, id, roomId, System
					.currentTimeMillis()));
		}
	}
}
