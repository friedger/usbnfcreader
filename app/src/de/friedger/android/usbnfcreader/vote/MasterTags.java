package de.friedger.android.usbnfcreader.vote;

import java.util.HashMap;
import java.util.Map;

public class MasterTags {

	private final static Map<String, VoteType> MASTER_TAGS = new HashMap<String, VoteType>();
	static {
		// Friedger Tags
		MASTER_TAGS.put("B22045EC", VoteType.POSITIVE);
		MASTER_TAGS.put("C2D646EC", VoteType.NEGATIVE);
		MASTER_TAGS.put("0DF099D8", VoteType.UNDEFINED);
		// Adrian Tags
		MASTER_TAGS.put("04E18FF24B2880", VoteType.POSITIVE);
		MASTER_TAGS.put("04CE8FF24B2880", VoteType.NEGATIVE);
		MASTER_TAGS.put("04968FF24B2880", VoteType.UNDEFINED);
	}

	public static VoteType identifyVoteTypeById(String id) {
		if (MASTER_TAGS.containsKey(id))
			return MASTER_TAGS.get(id);
		else
			return VoteType.UNDEFINED;
	}
}
