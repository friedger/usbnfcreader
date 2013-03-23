package de.friedger.android.usbnfcreader.vote;

import de.friedger.android.usbnfcreader.io.TagListener;

public class VotingMachine implements TagListener {

	private VoteManager voteManager;
	private VoteType voteType = VoteType.UNDEFINED;

	public VotingMachine(VoteManager voteManager) {
		this.voteManager = voteManager;		
	}
	
	public void setVoteType(VoteType voteType){
		this.voteType = voteType;
	}

	@Override
	public void onTag(String tagId) {
		if (voteType == VoteType.UNDEFINED){
			voteManager.configureMachine(this, tagId);
		} else {
			voteManager.submitVote(voteType, tagId);
		}
	}

	@Override
	public void onError(String message) {
		// TODO show error
	}
}
