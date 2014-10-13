package net.funkyjava.gametheory.cscfrm.model.game.nodes;

/**
 * The PlayerNode class.
 * 
 * @author Pierre Mardon
 */
public abstract class PlayerNode extends Node {

	/**
	 * The Constructor.
	 * 
	 * @param player
	 *            the player
	 * @param nbPlayerActions
	 *            the nb player actions
	 */
	protected PlayerNode(int player, int nbPlayerActions) {
		super(player, nbPlayerActions);
	}

	/**
	 * Gets the strategy sum.
	 * 
	 * @return the strategy sum
	 */
	public double[] getStrategySum() {
		return stratSum;
	}

	/**
	 * Gets the cumulative regret array.
	 * 
	 * @return the regret array
	 */
	public double[] getRegret() {
		return regretSum;
	}

	/**
	 * Computes and gets the average strategy.
	 * 
	 * @return the average strategy
	 */
	public double[] getAvgStrategy() {
		double tot = 0;
		for (int i = 0; i < stratSum.length; i++)
			tot += stratSum[i];
		final double[] res = new double[stratSum.length];
		for (int i = 0; i < stratSum.length; i++)
			res[i] = stratSum[i] / tot;
		return res;
	}

	/**
	 * Writes the average strategy in a destination array.
	 * 
	 * @param dest
	 *            destination array
	 */
	public void readAvgStrategy(double[] dest) {
		double tot = 0;
		for (int i = 0; i < stratSum.length; i++)
			tot += stratSum[i];
		for (int i = 0; i < stratSum.length; i++)
			dest[i] = stratSum[i] / tot;
	}

	/**
	 * Gets the realization weight sum.
	 * 
	 * @return the realization weight sum
	 */
	public double getRealWeightSum() {
		return realWeightSum;
	}

	/**
	 * Gets the visits count.
	 * 
	 * @return the visits count
	 */
	public long getVisitsCount() {
		return visits;
	}

	/**
	 * Gets the average realization weight.
	 * 
	 * @return the average realization weight
	 */
	public double getAvgRealWeight() {
		return realWeightSum / visits;
	}

}
