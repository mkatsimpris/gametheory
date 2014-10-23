package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import static com.google.common.base.Preconditions.checkArgument;
import lombok.Data;
import lombok.NonNull;

/**
 * Arguments for a {@link DoubleBucketer} to create buckets
 * 
 * @author Pierre Mardon
 * 
 */
@Data
public class DoubleBucketingArguments {
	private final double[][] values;
	private final int[] occurences;
	private final int nbBuckets;

	/**
	 * Constructor
	 * 
	 * @param values
	 *            the values for which we want to create buckets
	 * @param occurences
	 *            the absolute frequency of each value
	 * @param nbBuckets
	 *            the number of desired buckets. They will be represented by an
	 *            an integer between 0 and nbBuckets - 1
	 */
	public DoubleBucketingArguments(@NonNull double[][] values,
			@NonNull int[] occurences, int nbBuckets) {
		this.values = values;
		this.occurences = occurences;
		this.nbBuckets = nbBuckets;
		checkArgument(nbBuckets > 0, "Nb of buckets must be > 0");
		checkArgument(values.length == occurences.length,
				"The values and occurences array must have the same length");
	}

}
