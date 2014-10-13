package net.funkyjava.gametheory.cscfrm.core.engine;

/**
 * An extension of {@link CSCFRMUtilityManager} that provides proper
 * synchronization for multithreading and the {@link #read(double[])} method to
 * copy current utility safely.
 * 
 * @author Pierre Mardon
 */
public class CSCFRMMultithreadUtilityManager extends CSCFRMUtilityManager {

	/** The i :). */
	private int i;

	/**
	 * The Constructor.
	 * 
	 * @param nbPlayers
	 *            the nb players
	 */
	public CSCFRMMultithreadUtilityManager(int nbPlayers) {
		super(nbPlayers);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.engine.CSCFRMUtilityManager#addIterUtil(double[])
	 */
	@Override
	public synchronized void addIterUtil(double[] iterUtil) {
		super.addIterUtil(iterUtil);
	}

	/**
	 * Read the current utility in a synchronized way.
	 * 
	 * @param dest
	 *            the destination array
	 */
	public synchronized void read(double[] dest) {
		for (i = 0; i < nbPlayers; i++)
			dest[i] = util[i];
	}
}
