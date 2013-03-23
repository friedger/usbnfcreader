package de.friedger.android.usbnfcreader.vote;


public enum VoteType {
	UNDEFINED("0DF099D8"), POSITIVE ("B22045EC"), NEGATIVE("C2D646EC");
	
	private String mId;

	private VoteType(String id){
		mId = id;
	}
	
	public String getId(){
		return mId;
	}
	
	public static VoteType byId(String id){
		for (VoteType v : VoteType.values()){
			if (v.mId.equals(id)){
				return v;
			}
		}
		return VoteType.UNDEFINED;
	}
		
}
