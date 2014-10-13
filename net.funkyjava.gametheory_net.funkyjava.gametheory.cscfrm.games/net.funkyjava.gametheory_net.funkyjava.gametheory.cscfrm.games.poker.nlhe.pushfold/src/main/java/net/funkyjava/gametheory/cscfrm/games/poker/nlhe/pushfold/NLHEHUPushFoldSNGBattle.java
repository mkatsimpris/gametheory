package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.exe.CSCFRMCyclicStepsExecutor;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMultiThreadExecutor;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMFullGame;
import net.funkyjava.gametheory.play.CyclicStepsGameRunner;
import net.funkyjava.gametheory.play.GameRunnerArgs;

/**
 * This class instanciates two executors : one for basic NLHE HU push/fold poker game, the
 * other for the SNG cyclic steps game. It provides facilities to make the
 * associated games play again each other in a SNG battle.
 * 
 * @author Pierre Mardon
 */
public class NLHEHUPushFoldSNGBattle {

	private final int nbSteps, nbGamesBasic;

	/** The basic exe. */
	private final CSCFRMMultiThreadExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>>[] basicExe;

	/** The cyclic exe. */
	private final CSCFRMCyclicStepsExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> cyclicExe;

	/** The game runner with cyclic game as first player */
	private final CyclicStepsGameRunner runnerCB,
	/** The game runner with basic game as first player */
	runnerBC;

	/** Util sum for cyclic vs basic. */
	private final double[] utilSumCB,
	/** Util sum for basic vs cyclic */
	utilSumBC,
	/** Temporary utility array */
	utilTmp,
	/** Average utility */
	avgUtil;

	/** The number of games that were played for basic vs cyclic. */
	private int nbGamesBC = 0,
	/** The number of games that were played for cyclic vs basic. */
	nbGamesCB = 0;

	/**
	 * The Constructor.
	 * 
	 * @param workingDirectory
	 *            the working directory
	 * @param sb
	 *            the small blind
	 * @param bb
	 *            the big blind
	 * @param startingStack
	 *            the starting stack for each player
	 * @param granularity
	 *            the terminal nodes ids are indexed by (next game's small blind
	 *            stack / granularity) - 1. Sb, bb, and stacks must be multiple
	 *            of the granularity
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	@SuppressWarnings("unchecked")
	public NLHEHUPushFoldSNGBattle(Path workingDirectory, int sb, int bb,
			int startingStack, int granularity) throws IOException {
		checkNotNull(workingDirectory, "Please provide a working directory");
		NLHEHUPushFoldWorksStation ws = new NLHEHUPushFoldWorksStation(
				workingDirectory);
		cyclicExe = ws.buildSngExecutor(sb, bb, startingStack, granularity);
		List<NLHEHUPushFold<DefaultPlayerNode>> sngGames = cyclicExe.getGames();
		nbSteps = sngGames.size();
		Object[] basicGames = new Object[nbSteps];
		basicExe = new CSCFRMMultiThreadExecutor[nbSteps];
		nbGamesBasic = startingStack / granularity;
		for (int i = 0; i < nbGamesBasic; i++)
			basicExe[i] = ws.buildMultithreadExecutor(sb, bb, i * granularity
					+ granularity, 2 * startingStack
					- (i * granularity + granularity));
		for (int i = 0; i < nbSteps; i++)
			if (i < nbGamesBasic)
				basicGames[i] = basicExe[i].getGame();
			else
				basicGames[i] = basicExe[2 * nbGamesBasic - 2 - i].getGame();

		GameRunnerArgs[] argsCB = new GameRunnerArgs[nbSteps];
		GameRunnerArgs[] argsBC = new GameRunnerArgs[nbSteps];
		for (int i = 0; i < nbSteps; i++)
			argsCB[i] = new GameRunnerArgs(new CSCFRMFullGame<?>[] {
					sngGames.get(i),
					(NLHEHUPushFold<DefaultPlayerNode>) basicGames[i] });
		for (int i = 0; i < nbSteps; i++)
			argsBC[i] = new GameRunnerArgs(sngGames.get(i), sngGames.get(i),
					new CSCFRMFullGame<?>[] {
							(NLHEHUPushFold<DefaultPlayerNode>) basicGames[i],
							sngGames.get(i) });
		runnerCB = new CyclicStepsGameRunner(argsCB, new HUPositionMapper());
		runnerBC = new CyclicStepsGameRunner(argsBC, new HUPositionMapper());
		utilSumCB = new double[2];
		utilSumBC = new double[2];
		utilTmp = new double[2];
		avgUtil = new double[2];
	}

	/**
	 * Run cyclic versus basic games.
	 * 
	 * @param startingGame
	 *            index of the first step to play
	 * 
	 * @param nbGames
	 *            the number of games to play
	 */
	public synchronized void runCB(int startingGame, int nbGames) {
		for (int i = 0; i < nbGames; i++) {
			runnerCB.runOneGame(startingGame, utilTmp);
			utilSumCB[0] += utilTmp[0];
			utilSumCB[1] += utilTmp[1];
			nbGamesCB++;
		}
	}

	/**
	 * Run basic versus cyclic games.
	 * 
	 * @param startingGame
	 *            index of the first step to play
	 * 
	 * @param nbGames
	 *            the number of games to play
	 */
	public synchronized void runBC(int startingGame, int nbGames) {
		for (int i = 0; i < nbGames; i++) {
			runnerBC.runOneGame(startingGame, utilTmp);
			utilSumBC[0] += utilTmp[0];
			utilSumBC[1] += utilTmp[1];
			nbGamesBC++;
		}
	}

	/**
	 * Gets the avg utility in cyclic vs basic games.
	 * 
	 * @return the avg utility
	 */
	public double[] getAvgUtilCyclicBasic() {
		if (nbGamesCB > 0) {
			avgUtil[0] = utilSumCB[0] / nbGamesCB;
			avgUtil[1] = utilSumCB[1] / nbGamesCB;
		}
		return avgUtil;
	}

	/**
	 * Gets the avg utility in basic vs cyclic games.
	 * 
	 * @return the avg utility
	 */
	public double[] getAvgUtilBasicCyclic() {
		if (nbGamesBC > 0) {
			avgUtil[0] = utilSumBC[0] / nbGamesBC;
			avgUtil[1] = utilSumBC[1] / nbGamesBC;
		}
		return avgUtil;
	}

	/**
	 * Gets the number of basic vs cyclic games played.
	 * 
	 * @return the number of games
	 */
	public int getNbGamesBasicCyclic() {
		return nbGamesBC;
	}

	/**
	 * Gets the number of cyclic vs basic games played.
	 * 
	 * @return the number of games
	 */
	public int getNbGamesCyclicBasic() {
		return nbGamesCB;
	}

	/**
	 * Train the basic games for nbIter each game
	 * 
	 * @param nbIter
	 *            the number of iterations to perform
	 * @throws Exception
	 *             the exception
	 */
	public synchronized void trainBasic(int nbIter) throws Exception {
		for (int i = 0; i < nbGamesBasic; i++)
			basicExe[i].run(nbIter);
	}

	/**
	 * Train the cyclic kuhn poker.
	 * 
	 * @param nbIter
	 *            the number of iterations to perform
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public synchronized void trainCyclic(int nbIter)
			throws InterruptedException {
		cyclicExe.run(nbIter);
	}

	/**
	 * Save both cscfrm executions
	 * 
	 * @throws IOException
	 *             the IO exception
	 */
	public void save() throws IOException {
		for (int i = 0; i < nbSteps; i++)
			basicExe[i].save();
		cyclicExe.save();
	}

	/**
	 * Reset the play simulations.
	 */
	public void resetPlaySimulations() {
		nbGamesCB = 0;
		nbGamesBC = 0;
		utilSumCB[0] = 0;
		utilSumCB[1] = 0;
		utilSumBC[0] = 0;
		utilSumBC[1] = 0;
	}

	/**
	 * Gets the basic executor.
	 * 
	 * @return the basic exe
	 */
	CSCFRMMultiThreadExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>>[] getBasicExe() {
		return basicExe;
	}

	/**
	 * Gets the cyclic executor.
	 * 
	 * @return the cyclic exe
	 */
	CSCFRMCyclicStepsExecutor<DefaultPlayerNode, NLHEHUPushFold<DefaultPlayerNode>> getCyclicExe() {
		return cyclicExe;
	}
}
