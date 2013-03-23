package de.friedger.android.usbnfcreader.vote;

public class Vote {

	public static final String COLUMN_NAME_ROOM_ID = "room_id";
	public static final String COLUMN_NAME_VOTE_TYPE = "vote_type";
	public static final String COLUMN_NAME_ID = "id";
	public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
	
	
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
