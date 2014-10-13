package net.funkyjava.gametheory.cscfrm.exe;

import static com.google.common.base.Preconditions.checkArgument;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMMultithreadUtilityManager;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;

/**
 * A MonothreadCyclicUtilReader is an {@link CSCFRMTerminalUtilReader} that
 * handles position changes between the current game and that targets other
 * games utilities, mapping terminal states ids to the utility managers indexes.
 * It's intended to be provided to one engine only.
 * 
 * @author Pierre Mardon
 */
public class MonothreadCyclicUtilReader implements CSCFRMTerminalUtilReader {

	/** The number of players. */
	private final int nbPlayers;

	/** The target utility managers. */
	private final CSCFRMMultithreadUtilityManager[] utils;

	/** The position mapping array. */
	private final int[][] posMapping;

	/** A temp array. */
	private final double[] tmp;

	/** A simple variable. */
	private int fromPos;

	/**
	 * The Constructor.
	 * 
	 * @param nbPlayers
	 *            the number of players
	 * @param utils
	 *            the target utility managers. Their indexes are mapped to
	 *            terminal states ids.
	 * @param posMapping
	 *            the position mapping array : first index is the target
	 *            terminal state's id, the second is the position in the current
	 *            game, and the value should be the mapped position in the
	 *            target game.
	 */
	public MonothreadCyclicUtilReader(int nbPlayers,
			CSCFRMMultithreadUtilityManager[] utils, int[][] posMapping) {
		checkArgument(nbPlayers > 1, "The number of players must be > 0");
		this.nbPlayers = nbPlayers;
		this.utils = utils;
		tmp = new double[nbPlayers];
		this.posMapping = posMapping;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.funkyjava.cscfrm.engine.CSCFRMTerminalUtilReader#read(int,
	 * double[])
	 */
	@Override
	public void read(int id, double[] dest) throws InterruptedException {
		utils[id].read(tmp);
		for (fromPos = 0; fromPos < nbPlayers; fromPos++)
			dest[fromPos] = tmp[posMapping[id][fromPos]];
	}
}
