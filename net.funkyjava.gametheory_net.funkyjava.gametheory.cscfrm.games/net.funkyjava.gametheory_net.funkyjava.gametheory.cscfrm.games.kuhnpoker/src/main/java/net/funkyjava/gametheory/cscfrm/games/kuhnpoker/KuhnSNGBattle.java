package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import net.funkyjava.gametheory.cscfrm.exe.CSCFRMCyclicStepsExecutor;
import net.funkyjava.gametheory.cscfrm.exe.CSCFRMMonothreadExecutor;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMFullGame;
import net.funkyjava.gametheory.play.CyclicStepsGameRunner;
import net.funkyjava.gametheory.play.GameRunnerArgs;

/**
 * This class instanciates two executors : one for basic kuhn poker game, the
 * other for the SNG cyclic steps game. It provides facilities to make the
 * associated games play again each other in a SNG battle.
 * 
 * @author Pierre Mardon
 */
public class KuhnSNGBattle {

	/** The basic exe. */
	private final CSCFRMMonothreadExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> basicExe;

	/** The cyclic exe. */
	private final CSCFRMCyclicStepsExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> cyclicExe;

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

	private final int nbBlindsStack;

	/**
	 * The Constructor.
	 * 
	 * @param workingDirectory
	 *            the working directory
	 * @param nbBlindsStack
	 *            the number of blinds stack for the starting stacks in SNG
	 * @throws IOException
	 *             the IO exception
	 */
	public KuhnSNGBattle(Path workingDirectory, int nbBlindsStack)
			throws IOException {
		checkNotNull(workingDirectory, "Please provide a working directory");
		checkArgument(nbBlindsStack > 2,
				"The number of blinds in stacks must be at least 3");
		this.nbBlindsStack = nbBlindsStack;
		KuhnPokerWorksStation ws = new KuhnPokerWorksStation(workingDirectory);
		basicExe = ws.buildMonothreadExecutor();
		cyclicExe = ws.buildSngExecutor(nbBlindsStack);
		List<KuhnPoker<DefaultPlayerNode>> sngGames = cyclicExe.getGames();
		KuhnPoker<DefaultPlayerNode> basicGame = basicExe.getGame();
		int nbSteps = sngGames.size();
		GameRunnerArgs[] argsCB = new GameRunnerArgs[nbSteps];
		GameRunnerArgs[] argsBC = new GameRunnerArgs[nbSteps];
		for (int i = 0; i < nbSteps; i++)
			argsCB[i] = new GameRunnerArgs(new CSCFRMFullGame<?>[] {
					sngGames.get(i), basicGame });
		for (int i = 0; i < nbSteps; i++)
			argsBC[i] = new GameRunnerArgs(sngGames.get(i), sngGames.get(i),
					new CSCFRMFullGame<?>[] { basicGame, sngGames.get(i) });
		runnerCB = new CyclicStepsGameRunner(argsCB,
				new SNGKuhnPokerPositionMapper());
		runnerBC = new CyclicStepsGameRunner(argsBC,
				new SNGKuhnPokerPositionMapper());
		utilSumCB = new double[2];
		utilSumBC = new double[2];
		utilTmp = new double[2];
		avgUtil = new double[2];
	}

	/**
	 * Run cyclic versus basic games.
	 * 
	 * @param nbGames
	 *            the number of games to play
	 */
	public synchronized void runCB(int nbGames) {
		for (int i = 0; i < nbGames; i++) {
			runnerCB.runOneGame(nbBlindsStack - 3, utilTmp);
			utilSumCB[0] += utilTmp[0];
			utilSumCB[1] += utilTmp[1];
			nbGamesCB++;
		}
	}

	/**
	 * Run basic versus cyclic games.
	 * 
	 * @param nbGames
	 *            the number of games to play
	 */
	public synchronized void runBC(int nbGames) {
		for (int i = 0; i < nbGames; i++) {
			runnerBC.runOneGame(nbBlindsStack - 3, utilTmp);
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
	 * Train the basic kuhn poker.
	 * 
	 * @param nbIter
	 *            the number of iterations to perform
	 * @throws Exception
	 *             the exception
	 */
	public synchronized void trainBasic(int nbIter) throws Exception {
		basicExe.run(nbIter);
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
		basicExe.save();
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
	 * Gets the basic kuhn poker executor.
	 * 
	 * @return the basic exe
	 */
	CSCFRMMonothreadExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> getBasicExe() {
		return basicExe;
	}

	/**
	 * Gets the cyclic kuhn poker executor.
	 * 
	 * @return the cyclic exe
	 */
	CSCFRMCyclicStepsExecutor<DefaultPlayerNode, KuhnPoker<DefaultPlayerNode>> getCyclicExe() {
		return cyclicExe;
	}
}
