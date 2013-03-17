package de.friedger.android.usbnfcreader.vote;

import de.friedger.android.usbnfcreader.io.TagListener;

public class VotingMachine implements TagListener {

	private VoteManager voteManager;
	private VoteType voteType;

	public VotingMachine(VoteManager voteManager, VoteType voteType) {
		this.voteManager = voteManager;
		this.voteType = voteType;
	}

	@Override
	public void onTag(String tagId) {
		voteManager.submitVote(voteType, tagId);
	}
}
