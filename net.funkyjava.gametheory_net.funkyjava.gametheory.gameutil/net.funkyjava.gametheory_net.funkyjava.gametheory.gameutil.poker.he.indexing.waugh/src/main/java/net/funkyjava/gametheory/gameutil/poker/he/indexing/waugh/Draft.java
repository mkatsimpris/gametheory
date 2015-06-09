package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import org.apache.commons.math3.util.CombinatoricsUtils;

public class Draft {

	/**
	 * Number of ranks
	 */
	private final int n;

	public Draft(int n) {
		this.n = n;
	}

	private static int comb(int n, int k) {
		if (k > n)
			return 0;
		return (int) CombinatoricsUtils.binomialCoefficient(n, k);
	}

	/**
	 * 
	 * @param m
	 *            number of elements
	 * @param orderedRanks
	 *            the ranks to index
	 * @return the colex index
	 */
	private static int indexSet(int m, int[] orderedRanks) {
		assert (m == orderedRanks.length);
		int res = 0;
		for (int i = 0; i < m; i++)
			res += comb(orderedRanks[i], m - i);
		return res;
	}

	private void unindexSet(int idx, int m, int[] result) {
		unindexSet(idx, m, result, 0);
	}

	private void unindexSet(int idx, int m, int[] result, int offset) {
		if (offset == result.length)
			return;
		int x = findUnindexSetFirstRank(idx, m, offset == 0 ? n
				: result[offset - 1]);
		result[offset] = x;
		unindexSet(idx - comb(x, m), m - 1, result, offset + 1);
	}

	private int findUnindexSetFirstRank(int idx, int m, int upperBound) {
		int x = upperBound - 1;
		for (; comb(x, m) > idx; x--)
			;
		return x;
	}

	public static void main(String[] args) {
		Draft d = new Draft(52);
		int[] orderedRanks = { 13, 11, 9 };

	}
}
