package net.funkyjava.gametheory.gameutil.cards.indexing;

import static com.google.common.base.Preconditions.checkArgument;
import lombok.NonNull;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.IndexerBuckets;

/**
 * {@link CardsGroupsIndexer} built on top of another one and an
 * {@link IndexerBuckets}
 * 
 * @author Pierre Mardon
 * 
 */
public class CardsGroupBucketIndexer implements CardsGroupsIndexer {
	private final CardsGroupsIndexer baseIndexer;
	private final int[] buckets;
	private final int nbBuckets;

	/**
	 * Constructor
	 * 
	 * @param baseIndexer
	 *            the base indexer
	 * @param buckets
	 *            the buckets for the base indexer
	 */
	public CardsGroupBucketIndexer(@NonNull CardsGroupsIndexer baseIndexer,
			@NonNull IndexerBuckets buckets) {
		checkArgument(
				baseIndexer.getIndexSize() == buckets.getBuckets().length,
				"Buckets must be the same length as the indexer size !");
		this.baseIndexer = baseIndexer;
		this.buckets = buckets.getBuckets();
		this.nbBuckets = buckets.getNbBuckets();
	}

	@Override
	public int indexOf(int[][] cardsGroups) {
		return buckets[baseIndexer.indexOf(cardsGroups)];
	}

	@Override
	public int getIndexSize() {
		return nbBuckets;
	}

	@Override
	public IntCardsSpec getCardsSpec() {
		return baseIndexer.getCardsSpec();
	}

	@Override
	public boolean canHandleGroups(int[] groupsSizes) {
		return baseIndexer.canHandleGroups(groupsSizes);
	}

	@Override
	public boolean isCompatible(String gameId) {
		return baseIndexer.isCompatible(gameId);
	}

}
