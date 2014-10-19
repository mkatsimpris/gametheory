package net.funkyjava.gametheory.gameutil.cards.indexing.bucketting;

import lombok.Data;
import lombok.NonNull;

@Data
public class IndexerBuckets {
	private final int nbBuckets;
	@NonNull
	private final int[] buckets;

	public int getNbIndexes() {
		return buckets.length;
	}
}
