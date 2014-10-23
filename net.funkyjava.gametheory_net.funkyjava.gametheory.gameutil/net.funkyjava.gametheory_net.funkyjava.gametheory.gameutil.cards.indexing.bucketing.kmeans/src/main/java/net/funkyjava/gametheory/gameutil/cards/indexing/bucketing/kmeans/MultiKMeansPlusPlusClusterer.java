/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.kmeans;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.kmeans.KMeansPlusPlusClusterer.EmptyClusterStrategy;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.kmeans.distance.DistanceMeasure;

/**
 * A wrapper around a k-means++ clustering algorithm which performs multiple
 * trials and returns the best solution.
 * 
 * @param <T>
 *            type of the points to cluster
 * @version $Id: MultiKMeansPlusPlusClusterer.java 1462375 2013-03-29 01:42:42Z
 *          psteitz $
 * @since 3.2
 */
public class MultiKMeansPlusPlusClusterer<T extends Clusterable> extends
		Clusterer<T> {

	private final int k;
	private final int maxIterations;
	private final DistanceMeasure measure;
	private final EmptyClusterStrategy emptyStrategy;
	private final int numTrials;
	private final int parallelTrials;

	public MultiKMeansPlusPlusClusterer(final int k, final int maxIterations,
			final DistanceMeasure measure,
			final EmptyClusterStrategy emptyStrategy, final int numTrials,
			final int parallelTrials) {
		super(measure);
		this.k = k;
		this.maxIterations = maxIterations;
		this.measure = measure;
		this.emptyStrategy = emptyStrategy;
		this.numTrials = numTrials;
		this.parallelTrials = parallelTrials;
	}

	/**
	 * Returns the number of trials this instance will do.
	 * 
	 * @return the number of trials
	 */
	public int getNumTrials() {
		return numTrials;
	}

	@Override
	public List<CentroidCluster<T>> cluster(final Collection<T> points) {
		final AtomicLong bestVarianceSum = new AtomicLong();
		bestVarianceSum.set(Double.doubleToLongBits(Double.MAX_VALUE));
		final AtomicReference<List<CentroidCluster<T>>> selected = new AtomicReference<List<CentroidCluster<T>>>();
		final CountDownLatch latch = new CountDownLatch(numTrials);

		ExecutorService executor = Executors.newFixedThreadPool(parallelTrials);

		// do several clustering trials
		for (int i = 0; i < numTrials; ++i) {
			final int trial = i;
			executor.submit(new Runnable() {
				@Override
				public void run() {
					System.out.println("Starting trial " + trial);
					KMeansPlusPlusClusterer<T> clusterer = new KMeansPlusPlusClusterer<T>(
							k, maxIterations, measure, emptyStrategy, trial);

					// compute a clusters list
					List<CentroidCluster<T>> clusters = clusterer
							.cluster(points);

					// compute the variance of the current list
					double varianceSum = 0.0;
					for (final CentroidCluster<T> cluster : clusters) {
						if (!cluster.getPoints().isEmpty()) {

							// compute the distance variance of the current
							// cluster
							final Clusterable center = cluster.getCenter();
							double stat = 0;
							int n = 0;
							for (final T point : cluster.getPoints()) {
								final double dist = distance(point, center);
								stat += dist * dist * point.getCount();
								n += point.getCount();
							}
							varianceSum += (stat / n);
						}
					}
					System.out.println("T" + trial + ": Variance sum: "
							+ varianceSum);

					while (true) {
						final long oldBestVariance = bestVarianceSum.get();
						if (varianceSum >= Double
								.longBitsToDouble(oldBestVariance))
							break;
						if (bestVarianceSum.compareAndSet(oldBestVariance,
								Double.doubleToLongBits(varianceSum))) {
							selected.getAndSet(clusters);
							break;
						}
					}
					latch.countDown();
				}
			});
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		executor.shutdown();

		System.out.println("Best Variance: "
				+ Double.longBitsToDouble(bestVarianceSum.get()));

		// return the best clusters list found
		return selected.get();
	}

}
