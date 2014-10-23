package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import lombok.Data;
import lombok.NonNull;

/**
 * An indexer buckets contains buckets indexes for each indexed groups of cards
 * set
 * 
 * @author Pierre Mardon
 * 
 */
@Data
public class IndexerBuckets {
	private final int nbBuckets;
	@NonNull
	private final int[] buckets;

	/**
	 * Get the number of indexes for this bucketing
	 * 
	 * @return the number of indexes
	 */
	public int getNbIndexes() {
		return buckets.length;
	}
}
