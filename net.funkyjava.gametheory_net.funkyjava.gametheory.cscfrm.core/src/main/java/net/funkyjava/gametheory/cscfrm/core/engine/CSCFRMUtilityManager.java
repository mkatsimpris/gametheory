package net.funkyjava.gametheory.cscfrm.core.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Needed by {@link CSCFRMEngine} to handle game utility writing.
 * 
 * @author Pierre Mardon
 */
public class CSCFRMUtilityManager {

	/** The utility sum. */
	protected final double[] utilSum;

	/** The utility. */
	protected final double[] util;

	/** The number of players. */
	protected final int nbPlayers;

	/** The number of iterations. */
	protected long iter = 0;

	/** The i :). */
	private int i;

	/**
	 * The Constructor.
	 * 
	 * @param nbPlayers
	 *            the number of players
	 */
	public CSCFRMUtilityManager(int nbPlayers) {
		checkArgument(nbPlayers > 1, "The number of players must be > 1");
		this.nbPlayers = nbPlayers;
		this.utilSum = new double[nbPlayers];
		this.util = new double[nbPlayers];
	}

	/**
	 * Sets the engine's state.
	 * 
	 * @param state
	 *            the state
	 */
	public void setState(CSCFRMState state) {
		checkNotNull(state, "Cannot set a null state");
		checkArgument(state.getGameUtilSum().length == nbPlayers,
				"State's game util sum hasn't the right length, expected number of players");
		System.arraycopy(state.getGameUtilSum(), 0, utilSum, 0, nbPlayers);
		iter = state.getNbIter();
		if (iter > 0)
			for (int i = 0; i < nbPlayers; i++)
				util[i] = utilSum[i] / iter;
	}

	/**
	 * Gets the engine's state.
	 * 
	 * @return the state
	 */
	public CSCFRMState getState() {
		return new CSCFRMState(iter, utilSum);
	}

	/**
	 * Adds the iteration utility.
	 * 
	 * @param iterUtil
	 *            the iteration utility
	 */
	public void addIterUtil(double[] iterUtil) {
		iter++;
		for (i = 0; i < nbPlayers; i++)
			util[i] = (utilSum[i] += iterUtil[i]) / iter;
	}

	/**
	 * Gets the utility sum.
	 * 
	 * @return the utility sum
	 */
	public double[] getUtilSum() {
		return utilSum;
	}

	/**
	 * Gets the average utility.
	 * 
	 * @return the utility
	 */
	public double[] getUtil() {
		return util;
	}

	/**
	 * Gets number of iterations.
	 * 
	 * @return the number of iterations
	 */
	public long getIter() {
		return iter;
	}
}
