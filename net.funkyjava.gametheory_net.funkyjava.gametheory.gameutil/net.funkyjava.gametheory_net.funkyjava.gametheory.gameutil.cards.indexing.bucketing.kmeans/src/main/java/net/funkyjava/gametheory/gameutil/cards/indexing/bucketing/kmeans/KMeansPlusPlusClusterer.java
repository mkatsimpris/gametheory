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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.kmeans.distance.DistanceMeasure;
import net.funkyjava.gametheory.gameutil.cards.indexing.bucketing.kmeans.distance.EuclideanDistance;

/**
 * Clustering algorithm based on David Arthur and Sergei Vassilvitski k-means++
 * algorithm.
 * 
 * @param <T>
 *            type of the points to cluster
 * @see <a href="http://en.wikipedia.org/wiki/K-means%2B%2B">K-means++
 *      (wikipedia)</a>
 * @version $Id: KMeansPlusPlusClusterer.java 1461866 2013-03-27 21:54:36Z tn $
 * @since 3.2
 */
public class KMeansPlusPlusClusterer<T extends Clusterable> extends Clusterer<T> {

	/** Strategies to use for replacing an empty cluster. */
	public static enum EmptyClusterStrategy {

		/** Split the cluster with largest distance variance. */
		LARGEST_VARIANCE,

		/** Split the cluster with largest number of points. */
		LARGEST_POINTS_NUMBER,

		/** Create a cluster around the point farthest from its centroid. */
		FARTHEST_POINT,

		/** Generate an error. */
		ERROR

	}

	/** The number of clusters. */
	protected final int k;

	/** The maximum number of iterations. */
	private final int maxIterations;

	/** Random generator for choosing initial centers. */
	private final Random random = new Random();

	/** Selected strategy for empty clusters. */
	private final EmptyClusterStrategy emptyStrategy;
	
	private final int trial;

	/**
	 * Build a clusterer.
	 * <p>
	 * The default strategy for handling empty clusters that may appear during
	 * algorithm iterations is to split the cluster with largest distance
	 * variance.
	 * <p>
	 * The euclidean distance will be used as default distance measure.
	 * 
	 * @param k
	 *            the number of clusters to split the data into
	 */
	public KMeansPlusPlusClusterer(final int k) {
		this(k, -1);
	}

	/**
	 * Build a clusterer.
	 * <p>
	 * The default strategy for handling empty clusters that may appear during
	 * algorithm iterations is to split the cluster with largest distance
	 * variance.
	 * <p>
	 * The euclidean distance will be used as default distance measure.
	 * 
	 * @param k
	 *            the number of clusters to split the data into
	 * @param maxIterations
	 *            the maximum number of iterations to run the algorithm for. If
	 *            negative, no maximum will be used.
	 */
	public KMeansPlusPlusClusterer(final int k, final int maxIterations) {
		this(k, maxIterations, new EuclideanDistance());
	}

	/**
	 * Build a clusterer.
	 * <p>
	 * The default strategy for handling empty clusters that may appear during
	 * algorithm iterations is to split the cluster with largest distance
	 * variance.
	 * 
	 * @param k
	 *            the number of clusters to split the data into
	 * @param maxIterations
	 *            the maximum number of iterations to run the algorithm for. If
	 *            negative, no maximum will be used.
	 * @param measure
	 *            the distance measure to use
	 */
	public KMeansPlusPlusClusterer(final int k, final int maxIterations,
			final DistanceMeasure measure) {
		this(k, maxIterations, measure, EmptyClusterStrategy.LARGEST_VARIANCE);
	}

	/**
	 * Build a clusterer.
	 * 
	 * @param k
	 *            the number of clusters to split the data into
	 * @param maxIterations
	 *            the maximum number of iterations to run the algorithm for. If
	 *            negative, no maximum will be used.
	 * @param measure
	 *            the distance measure to use
	 * @param emptyStrategy
	 *            strategy to use for handling empty clusters that may appear
	 *            during algorithm iterations
	 */
	public KMeansPlusPlusClusterer(final int k, final int maxIterations,
			final DistanceMeasure measure,
			final EmptyClusterStrategy emptyStrategy) {
		this(k, maxIterations, measure, EmptyClusterStrategy.LARGEST_VARIANCE, 0);
	}
	
	public KMeansPlusPlusClusterer(final int k, final int maxIterations,
			final DistanceMeasure measure,
			final EmptyClusterStrategy emptyStrategy, final int trial) {
		super(measure);
		this.k = k;
		this.maxIterations = maxIterations;
		this.emptyStrategy = emptyStrategy;
		this.trial = trial;
	}

	/**
	 * Return the number of clusters this instance will use.
	 * 
	 * @return the number of clusters
	 */
	public int getK() {
		return k;
	}

	/**
	 * Returns the maximum number of iterations this instance will use.
	 * 
	 * @return the maximum number of iterations, or -1 if no maximum is set
	 */
	public int getMaxIterations() {
		return maxIterations;
	}

	/**
	 * Returns the {@link EmptyClusterStrategy} used by this instance.
	 * 
	 * @return the {@link EmptyClusterStrategy}
	 */
	public EmptyClusterStrategy getEmptyClusterStrategy() {
		return emptyStrategy;
	}

	public List<CentroidCluster<T>> cluster(final Collection<T> points) {

		// number of clusters has to be smaller or equal the number of data
		// points
		if (points.size() < k) {
			// TODO: Exception
			return null;
		}

		// Convert to list for indexed access. Make it unmodifiable, since
		// removal of items
		// would screw up the logic of this method.
		final List<T> pointList = Collections.unmodifiableList(new ArrayList<T>(points));

		// create the initial clusters
		ArrayList<CentroidCluster<T>> clusters = chooseInitialCenters(pointList);

		// create an array containing the latest assignment of a point to a
		// cluster
		// no need to initialize the array, as it will be filled with the first
		// assignment
		int[] assignments = new int[points.size()];
		assignPointsToClusters(clusters, pointList, assignments);

		System.out.println("T" + trial + ": Starting iterations");
		final long startTime = System.currentTimeMillis();
		long time = startTime;

		// iterate through updating the centers until we're done
		final int max = (maxIterations < 0) ? Integer.MAX_VALUE : maxIterations;
		for (int count = 0; count < max; count++) {
			boolean emptyCluster = false;
			ArrayList<CentroidCluster<T>> newClusters = new ArrayList<CentroidCluster<T>>();
			for (final CentroidCluster<T> cluster : clusters) {
				final Clusterable newCenter;
				if (cluster.getPoints().isEmpty()) {
					switch (emptyStrategy) {
					case LARGEST_VARIANCE:
						newCenter = getPointFromLargestVarianceCluster(clusters);
						break;
					case LARGEST_POINTS_NUMBER:
						newCenter = getPointFromLargestNumberCluster(clusters);
						break;
					case FARTHEST_POINT:
						newCenter = getFarthestPoint(clusters);
						break;
					default:
						throw new RuntimeException();
					}
					emptyCluster = true;
				} else {
					newCenter = centroidOf(cluster.getPoints(), cluster
							.getCenter().getPoint().length);
				}
				newClusters.add(new CentroidCluster<T>(newCenter));
			}
			int changes = assignPointsToClusters(newClusters, pointList, assignments);
			clusters = newClusters;

			System.out.printf("T%2d: %4d | %8d changes | %6d ms | %5d s\n",
					trial, (count + 1), changes, System.currentTimeMillis() - time, (System.currentTimeMillis() - startTime)/1000);
			time = System.currentTimeMillis();
			
			// if there were no more changes in the point-to-cluster assignment
			// and there are no empty clusters left, return the current clusters
			if (changes == 0 && !emptyCluster) {
				break;
			}
		}
		return clusters;
	}

	/**
	 * Adds the given points to the closest {@link Cluster}.
	 * 
	 * @param clusters
	 *            the {@link Cluster}s to add the points to
	 * @param points
	 *            the points to add to the given {@link Cluster}s
	 * @param assignments
	 *            points assignments to clusters
	 * @return the number of points assigned to different clusters as the
	 *         iteration before
	 */
	private int assignPointsToClusters(final ArrayList<CentroidCluster<T>> clusters, final List<T> pointList, final int[] assignments) {
		int assignedDifferently = 0;
		int pointIndex = 0;
		for (final T p : pointList) {
			int clusterIndex = getNearestCluster(clusters, p);
			if (clusterIndex != assignments[pointIndex]) {
				assignedDifferently++;
			}
			clusters.get(clusterIndex).addPoint(p);
			assignments[pointIndex++] = clusterIndex;
		}

        return assignedDifferently;
	}

	/**
	 * Use K-means++ to choose the initial centers.
	 * 
	 * @param points
	 *            the points to choose the initial centers from
	 * @return the initial centers
	 */
	private ArrayList<CentroidCluster<T>> chooseInitialCenters(
			final List<T> pointList) {

		System.out.println("T" + trial + ": Choosing initial centers");

		// The number of points in the list.
		final int numPoints = pointList.size();

		// Set the corresponding element in this array to indicate when
		// elements of pointList are no longer available.
		final boolean[] taken = new boolean[numPoints];

		// The resulting list of initial centers.
		final ArrayList<CentroidCluster<T>> resultSet = new ArrayList<CentroidCluster<T>>();

		// Choose one center uniformly at random from among the data points.
		final int firstPointIndex = random.nextInt(numPoints);

		final T firstPoint = pointList.get(firstPointIndex);

		resultSet.add(new CentroidCluster<T>(firstPoint));

		// Must mark it as taken
		taken[firstPointIndex] = true;

		// To keep track of the minimum distance squared of elements of
		// pointList to elements of resultSet.
		final double[] minDistSquared = new double[numPoints];

		// Initialize the elements. Since the only point in resultSet is
		// firstPoint,
		// this is very easy.
		for (int i = 0; i < numPoints; i++) {
			if (i != firstPointIndex) { // That point isn't considered
				final T point = pointList.get(i);
				double d = distance(firstPoint, point);
				minDistSquared[i] = d * d * point.getCount();
			}
		}

		while (resultSet.size() < k) {

			// Sum up the squared distances for the points in pointList not
			// already taken.
			double distSqSum = 0.0;

			for (int i = 0; i < numPoints; i++) {
				if (!taken[i]) {
					distSqSum += minDistSquared[i];
				}
			}

			// Add one new data point as a center. Each point x is chosen with
			// probability proportional to D(x)2
			final double r = random.nextDouble() * distSqSum;

			// The index of the next point to be added to the resultSet.
			int nextPointIndex = -1;

			// Sum through the squared min distances again, stopping when
			// sum >= r.
			double sum = 0.0;
			for (int i = 0; i < numPoints; i++) {
				if (!taken[i]) {
					sum += minDistSquared[i];
					if (sum >= r) {
						nextPointIndex = i;
						break;
					}
				}
			}

			// If it's not set to >= 0, the point wasn't found in the previous
			// for loop, probably because distances are extremely small. Just
			// pick
			// the last available point.
			if (nextPointIndex == -1) {
				for (int i = numPoints - 1; i >= 0; i--) {
					if (!taken[i]) {
						nextPointIndex = i;
						break;
					}
				}
			}

			// We found one.
			if (nextPointIndex >= 0) {

				final T p = pointList.get(nextPointIndex);

				resultSet.add(new CentroidCluster<T>(p));

				// Mark it as taken.
				taken[nextPointIndex] = true;

				if (resultSet.size() < k) {
					// Now update elements of minDistSquared. We only have to
					// compute
					// the distance to the new center to do this.
					/*
					 * for (int j = 0; j < numPoints; j++) { // Only have to
					 * worry about the points still not taken. if (!taken[j]) {
					 * final T point = pointList.get(j); double d = distance(p,
					 * point); double d2 = d * d * point.getCount(); if (d2 <
					 * minDistSquared[j]) { minDistSquared[j] = d2; } } }
					 */

					for (int j = 0; j < numPoints; j++) {
						if (!taken[j]) {
							final T point = pointList.get(j);
							double d = distance(p, point);
							double d2 = d * d * point.getCount();
							if (d2 < minDistSquared[j]) {
								minDistSquared[j] = d2;
							}
						}
					}
				}

			} else {
				// None found --
				// Break from the while loop to prevent
				// an infinite loop.
				break;
			}
		}
		System.out.println("T" + trial + ": Choosing initial centers ready!");

		return resultSet;
	}

	/**
	 * Get a random point from the {@link Cluster} with the largest distance
	 * variance.
	 * 
	 * @param clusters
	 *            the {@link Cluster}s to search
	 * @return a random point from the selected cluster
	 * @throws ConvergenceException
	 *             if clusters are all empty
	 */
	private T getPointFromLargestVarianceCluster(
			final ArrayList<CentroidCluster<T>> clusters) {

		double maxVariance = Double.NEGATIVE_INFINITY;
		Cluster<T> selected = null;

		for (final CentroidCluster<T> cluster : clusters) {
			if (!cluster.getPoints().isEmpty()) {
				// compute the distance variance of the current cluster
				final Clusterable center = cluster.getCenter();
				double variance = 0;
				int n = 0;
				for (final T point : cluster.getPoints()) {
					final double dist = distance(point, center);
					variance += dist * dist * point.getCount();
					n += point.getCount();
				}
				variance /= n;

				// select the cluster with the largest variance
				if (variance > maxVariance) {
					selected = cluster;
					maxVariance = variance;
				}
			}
		}

		// did we find at least one non-empty cluster ?
		if (selected == null) {
			throw new RuntimeException();
		}

		// extract a random point from the cluster
		final List<T> selectedPoints = selected.getPoints();
		return selectedPoints.remove(random.nextInt(selectedPoints.size()));

	}

	/**
	 * Get a random point from the {@link Cluster} with the largest number of
	 * points
	 * 
	 * @param clusters
	 *            the {@link Cluster}s to search
	 * @return a random point from the selected cluster
	 * @throws ConvergenceException
	 *             if clusters are all empty
	 */
	private T getPointFromLargestNumberCluster(
			final Collection<? extends Cluster<T>> clusters) {

		int maxNumber = 0;
		Cluster<T> selected = null;
		for (final Cluster<T> cluster : clusters) {

			// get the number of points of the current cluster
			final int number = cluster.getPoints().size();

			// select the cluster with the largest number of points
			if (number > maxNumber) {
				maxNumber = number;
				selected = cluster;
			}

		}

		// did we find at least one non-empty cluster ?
		if (selected == null) {
			throw new RuntimeException();
		}

		// extract a random point from the cluster
		final List<T> selectedPoints = selected.getPoints();
		return selectedPoints.remove(random.nextInt(selectedPoints.size()));

	}

	/**
	 * Get the point farthest to its cluster center
	 * 
	 * @param clusters
	 *            the {@link Cluster}s to search
	 * @return point farthest to its cluster center
	 * @throws ConvergenceException
	 *             if clusters are all empty
	 */
	private T getFarthestPoint(final Collection<CentroidCluster<T>> clusters) {

		double maxDistance = Double.NEGATIVE_INFINITY;
		Cluster<T> selectedCluster = null;
		int selectedPoint = -1;
		for (final CentroidCluster<T> cluster : clusters) {

			// get the farthest point
			final Clusterable center = cluster.getCenter();
			final List<T> points = cluster.getPoints();
			for (int i = 0; i < points.size(); ++i) {
				final double distance = distance(points.get(i), center);
				if (distance > maxDistance) {
					maxDistance = distance;
					selectedCluster = cluster;
					selectedPoint = i;
				}
			}

		}

		// did we find at least one non-empty cluster ?
		if (selectedCluster == null) {
			throw new RuntimeException();
		}

		return selectedCluster.getPoints().remove(selectedPoint);

	}

	/**
	 * Returns the nearest {@link Cluster} to the given point
	 * 
	 * @param clusters
	 *            the {@link Cluster}s to search
	 * @param point
	 *            the point to find the nearest {@link Cluster} for
	 * @return the index of the nearest {@link Cluster} to the given point
	 */
	private int getNearestCluster(final Collection<CentroidCluster<T>> clusters, final T point) {
		double minDistance = Double.MAX_VALUE;
		int clusterIndex = 0;
		int minCluster = 0;
		for (final CentroidCluster<T> c : clusters) {
			final double distance = distance(point, c.getCenter());
			if (distance < minDistance) {
				minDistance = distance;
				minCluster = clusterIndex;
			}
			clusterIndex++;
		}
		return minCluster;
	}

	/**
	 * Computes the centroid for a set of points.
	 * 
	 * @param points
	 *            the set of points
	 * @param dimension
	 *            the point dimension
	 * @return the computed centroid for the set of points
	 */
	protected Clusterable centroidOf(final Collection<T> points,
			final int dimension) {
		final double[] centroid = new double[dimension];
		int numberOfPoints = 0;
		for (final T p : points) {
			final double[] point = p.getPoint();
			final int count = p.getCount();
			numberOfPoints += count;
			for (int i = 0; i < centroid.length; i++) {
				centroid[i] += point[i] * count;
			}
		}
		for (int i = 0; i < centroid.length; i++) {
			centroid[i] /= numberOfPoints;
		}
		return new DoublePoint(centroid);
	}

}
