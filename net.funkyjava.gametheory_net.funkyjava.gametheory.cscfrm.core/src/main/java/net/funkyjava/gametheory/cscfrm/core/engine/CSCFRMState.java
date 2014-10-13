package net.funkyjava.gametheory.cscfrm.core.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * CSCFRM state class that describes a {@link CSCFRMEngine} state.
 * 
 * @author Pierre Mardon
 */
public class CSCFRMState {

	/** The nb iter. */
	private final long nbIter;

	/** The game util. */
	private final double[] gameUtilSum, gameUtil;

	/**
	 * The Constructor.
	 * 
	 * @param nbIter
	 *            the nb iter
	 * @param gameUtilSum
	 *            the game util sum
	 */
	public CSCFRMState(long nbIter, double[] gameUtilSum) {
		checkNotNull(gameUtilSum, "The game util sum cannot be null");
		checkArgument(nbIter >= 0, "The number of iterations is negative");
		checkArgument(gameUtilSum.length > 1, "Game util sum length is 0");
		this.nbIter = nbIter;
		this.gameUtilSum = gameUtilSum;
		this.gameUtil = new double[gameUtilSum.length];
		if (nbIter > 0)
			for (int i = 0; i < gameUtilSum.length; i++)
				gameUtil[i] = gameUtilSum[i] / nbIter;
	}

	/**
	 * Gets the iterations number.
	 * 
	 * @return the iterations number
	 */
	public long getNbIter() {
		return nbIter;
	}

	/**
	 * Gets the game utility sum.
	 * 
	 * @return the game utility sum
	 */
	public double[] getGameUtilSum() {
		return gameUtilSum;
	}

	/**
	 * Gets the game utility.
	 * 
	 * @return the game utility
	 */
	public double[] getGameUtil() {
		return gameUtil;
	}
}