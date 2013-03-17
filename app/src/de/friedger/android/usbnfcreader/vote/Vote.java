package de.friedger.android.usbnfcreader.vote;

public class Vote {

	private VoteType voteType;
	private String id;
	private String roomId;
	private long timestamp;

	public Vote(VoteType voteType, String id, String roomId, long timestamp) {
		this.voteType = voteType;
		this.id = id;
		this.roomId = roomId;
		this.timestamp = timestamp;
	}

	public VoteType getVoteType() {
		return voteType;
	}

	public String getId() {
		return id;
	}

	public String getRoomId() {
		return roomId;
	}

	public long getTimestamp() {
		return timestamp;
	}
}
