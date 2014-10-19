package net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.kmeans;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.DoubleBucketer;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.DoubleBucketingArguments;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.IndexerBuckets;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.kmeans.KMeansPlusPlusClusterer.EmptyClusterStrategy;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketting.kmeans.distance.EarthMoversDistance;

public class KMeansPlusPlusBucketer implements DoubleBucketer {

	private final int nbThreads, maxIteration, numTrials;

	public KMeansPlusPlusBucketer(int nbThreads, int maxIteration, int numTrials) {
		this.nbThreads = nbThreads;
		this.maxIteration = maxIteration;
		this.numTrials = numTrials;
	}

	@Override
	public synchronized IndexerBuckets getBuckets(DoubleBucketingArguments args) {
		final int k = args.getNbBuckets();
		MultiKMeansPlusPlusClusterer<DoublePoint> clusterer = new MultiKMeansPlusPlusClusterer<DoublePoint>(
				k, maxIteration, new EarthMoversDistance(),
				EmptyClusterStrategy.LARGEST_VARIANCE, numTrials, nbThreads);
		final List<DoublePoint> points = new LinkedList<>();
		for (int i = 0; i < args.getValues().length; i++)
			points.add(new DoublePoint(args.getOccurences()[i], args
					.getValues()[i]));
		List<CentroidCluster<DoublePoint>> clusters = clusterer.cluster(points);
		int[] valuesBuckets = new int[args.getValues().length];
		int i = 0;
		for (CentroidCluster<DoublePoint> cluster : clusters) {
			pointloop: for (DoublePoint point : cluster.getPoints()) {
				for (int j = 0; j < args.getValues().length; j++)
					if (points.get(j) == point) {
						valuesBuckets[j] = i;
						continue pointloop;
					}
			}
			i++;
		}
		return new IndexerBuckets(args.getNbBuckets(), valuesBuckets);
	}
}
