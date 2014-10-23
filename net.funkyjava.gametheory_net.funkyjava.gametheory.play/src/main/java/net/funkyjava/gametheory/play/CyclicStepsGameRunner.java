package net.funkyjava.gametheory.play;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;
import net.funkyjava.gametheory.commonmodel.game.GameTreeWalker;
import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;

/**
 * <p>
 * Runs games on cyclic steps game. Each step has a dedicated {@link GameRunner}
 * built with the constructor {@link GameRunnerArgs} arguments, and position
 * changes are handled by a {@link CyclicStepsGamePositionMapper}.
 * </p>
 * <p>
 * The method {@link MinPublicNode#getId()} of terminal nodes provided by each
 * of the {@link GameTreeWalker} are mapped to step games indexes in the array
 * of {@link GameRunnerArgs} provided to the constructor.
 * </p>
 * 
 * @author Pierre Mardon
 * 
 * @see CyclicStepsGamePositionMapper
 * @see GameRunner
 * @see GameRunnerArgs
 */

@Slf4j
public class CyclicStepsGameRunner {

	/** The runners. */
	private final GameRunner[] runners;

	/** The position mapper. */
	private final CyclicStepsGamePositionMapper posMapper;

	/** The number of players. */
	private final int nbPlayers;

	/** Temporary arrays for position and deciders permutations. */
	private final int[] pos, decidersPerm;

	/** Temporary utility array. */
	private final double[] tmpUtil;

	/** The i :). */
	private int i;

	/**
	 * The Constructor.
	 * 
	 * @param stepsGames
	 *            the steps games
	 * @param posMapper
	 *            the position mapper
	 */
	public CyclicStepsGameRunner(GameRunnerArgs[] stepsGames,
			CyclicStepsGamePositionMapper posMapper) {
		checkNotNull(stepsGames, "Steps games array is null");
		checkNotNull(posMapper, "Positions mapper is null");
		checkArgument(stepsGames.length > 0, "Steps games array is empty");
		runners = new GameRunner[stepsGames.length];
		for (i = 0; i < stepsGames.length; i++)
			runners[i] = new GameRunner(checkNotNull(stepsGames[i],
					"Step %s is null", i));
		this.nbPlayers = stepsGames[0].getDeciders().length;
		checkArgument(nbPlayers > 1,
				"Number of deciders (players) must be at least 2");
		for (i = 0; i < stepsGames.length; i++)
			checkArgument(nbPlayers == stepsGames[0].getDeciders().length,
					"Steps games don't have all the same number of deciders (players).");
		pos = new int[nbPlayers];
		decidersPerm = new int[nbPlayers];
		tmpUtil = new double[nbPlayers];
		this.posMapper = posMapper;
		log.info(
				"Initialized CyclicStepsGameRunner for {} steps and {} players",
				stepsGames.length, nbPlayers);
	}

	/**
	 * Run one game.
	 * 
	 * @param startingGame
	 *            index of the first step to play
	 * 
	 * @param utilDest
	 *            the utility destination where final payoffs will be copied
	 *            according to all steps position mapping
	 */
	public void runOneGame(int startingGame, double[] utilDest) {
		final boolean debug = log.isDebugEnabled();
		if (debug)
			log.debug(
					"Running one game over cyclic steps, starting with step {}",
					startingGame);
		int step;
		int nextStep = startingGame;
		for (i = 0; i < nbPlayers; i++) {
			pos[i] = i;
			decidersPerm[i] = i;
		}
		while (true) {
			step = nextStep;
			if (debug)
				log.debug("Playing step {}", step);
			nextStep = runners[step].runOneGame(tmpUtil);
			if (nextStep < 0)
				break;
			for (i = 0; i < nbPlayers; i++) {
				pos[i] = posMapper
						.getNextPlayerPosition(step, nextStep, pos[i]);
				decidersPerm[pos[i]] = i;
			}
		}
		for (i = 0; i < nbPlayers; i++)
			utilDest[i] = tmpUtil[pos[i]];
		if (debug)
			log.debug("Game payoffs : {}", utilDest);
	}
}
