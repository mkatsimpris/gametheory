package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

/**
 * A bucketer intends to group values into buckets depending on values.
 * 
 * @author Pierre Mardon
 * 
 */
public interface DoubleBucketer {

	/**
	 * Expects the same number of values for each index. The return value must
	 * contain the resulting buckets for each values couple index.
	 * 
	 * @param arguments
	 *            data to perform bucketing
	 * 
	 * @return resulting buckets
	 */
	IndexerBuckets getBuckets(DoubleBucketingArguments arguments);

}
