package net.funkyjava.gametheory.cscfrm.core.engine;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMBaseGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;

/**
 * Chance-Sampling Counter Factual Regret Minimization algorithm implementation
 * 
 * @author Pierre Mardon
 */
public final class CSCFRMEngine {

	/** The game on which the engine will run. */
	private final CSCFRMBaseGame game;

	/** The number of players. */
	private final int nbPlayers;

	/** Iteration depth. */
	private int depth = 0;
	/** Iteration next depth. */
	private int nextDepth;

	/** The node variable. */
	private Node node;

	/** Iteration nodes indexed by depth. */
	private final Node[] iterNodes;

	/** Last action evaluated for player nodes only, indexed by depth. */
	private final int[] iterNodeLastAction;

	/** The realization weights for player nodes only, indexed by depth. */
	private final double[][] realizationWeight;

	/** The utility for iteration nodes, indexed by depth. */
	private final double[][] util;

	/** The player nodes utility for each action, indexed by depth. */
	private final double[][][] pNodesUtil;

	/** The player nodes iteration strategies indexed by depth. */
	private final double[][] strategies;

	/** The zero array to reset utility. */
	private final double[] zero;

	/**
	 * The forward boolean that indicates in which direction the iteration goes.
	 */
	private boolean forward = true;

	/** Iteration utility. */
	private double[] itUtil;
	/** Iteration next utility. */
	private double[] itNextUtil;
	/** Iteration strategy. */
	private double[] itStrat;
	/** Iteration regret. */
	private double[] itRegret;
	/** Iteration realization weight. */
	private double[] itReal;
	/** Iteration next realization weight. */
	private double[] itNextReal;
	/** Iteration strategy sum. */
	private double[] stratSum;

	/** Temporary weight double. */
	private double weight;
	/** Temporary totalRegret double. */
	private double totalRegret;

	/** Temporary int. */
	private int a;
	/** Temporary int. */
	private int i;
	/** Temporary int. */
	private int p;
	/** Temporary int. */
	private int player;
	/** Temporary int. */
	private int action;
	/** Temporary int. */
	private int iter;
	/** Temporary int. */
	private int nbActions;

	/** The engine's utility manager. */
	private CSCFRMUtilityManager utilMgr;

	/** The terminal utility reader. */
	private CSCFRMTerminalUtilReader termUtils;

	/** Boolean to indicate if the engine must lock player nodes. */
	private boolean lockPlayersNodes = true;

	/**
	 * Indicates whether the engine must update player nodes visits and
	 * realization weight or not.
	 */
	private boolean updateVisits = true;

	/**
	 * Boolean to indicate if the engine must read the utility of terminal nodes
	 * with an id >= 0 from {@link #termUtils}.
	 */
	private boolean readTerminalUtil = false;

	/**
	 * The Constructor.
	 * 
	 * @param game
	 *            the game
	 */
	public CSCFRMEngine(CSCFRMBaseGame game) {
		this.game = checkNotNull(game, "The game cannot be null");
		checkArgument(game.getNbPlayers() > 1,
				"Game must have at least two players");
		this.nbPlayers = game.getNbPlayers();
		checkArgument(game.getMaxDepth() > 0, "Game's max depth must be > 0");
		depth = game.getMaxDepth();
		zero = new double[nbPlayers];
		iterNodes = new Node[depth];
		iterNodeLastAction = new int[depth];
		util = new double[depth][nbPlayers];
		realizationWeight = new double[depth][nbPlayers];
		pNodesUtil = new double[depth][game.getMaxNbPlActions()][nbPlayers];
		utilMgr = new CSCFRMUtilityManager(nbPlayers);
		strategies = new double[depth][game.getMaxNbPlActions()];
	}

	/**
	 * Sets the config.
	 * 
	 * @param config
	 *            the config
	 */
	public void setConfig(CSCFRMConfig config) {
		checkNotNull(config, "Trying to set a null configuration to engine");
		this.lockPlayersNodes = config.isLockPlayerNodes();
		updateVisits = config.isUpdateVisitsAndWeight();
		this.termUtils = config.getTermUtilReader();
		this.readTerminalUtil = termUtils != null;
		if (config.getUtilityManager() != null)
			this.utilMgr = config.getUtilityManager();
	}

	/**
	 * Train.
	 * 
	 * @param nbIter
	 *            the number of iterations to perform
	 * @throws Exception
	 *             any exception that can be caused by a malformed game
	 */
	public void train(final int nbIter) throws Exception {
		for (iter = 0; iter < nbIter; iter++) {
			forward = true;
			depth = -1;
			for (i = 0; i < nbPlayers; i++)
				realizationWeight[0][i] = 1;
			game.onIterationStart();
			while (true) {
				if (forward) {
					nextDepth = (++depth) + 1;
					iterNodes[depth] = node = game.getCurrentNode();
					if (node.bType == 2) {
						// Terminal
						forward = false;
						if (!readTerminalUtil || (a = node.id) < 0) {
							System.arraycopy(node.payoffs, 0,
									itUtil = util[depth], 0, nbPlayers);
							if (depth > 0)
								continue;
							break;
						}
						termUtils.read(a, itUtil = util[depth]);
						if (depth > 0)
							continue;
						break;
					}
					itUtil = util[depth];
					System.arraycopy(zero, 0, itUtil, 0, nbPlayers);
					if (node.bType == 0) { // Chance
						System.arraycopy(realizationWeight[depth], 0,
								realizationWeight[nextDepth], 0, nbPlayers);
						game.choseChanceAction();
						continue;
					}
					// Player
					iterNodeLastAction[depth] = 0;
					totalRegret = 0;
					itRegret = node.regretSum;
					itStrat = strategies[depth];
					player = node.player;
					nbActions = (stratSum = node.stratSum).length;
					weight = (itReal = realizationWeight[depth])[player];
					System.arraycopy(itReal, 0,
							itNextReal = realizationWeight[nextDepth], 0,
							nbPlayers);
					if (lockPlayersNodes)
						node.lock();
					for (a = 0; a < nbActions; a++)
						totalRegret += itStrat[a] = (itRegret[a] > 0 ? itRegret[a]
								: 0);
					if (totalRegret > 0)
						for (a = 0; a < nbActions; a++)
							stratSum[a] += weight * (itStrat[a] /= totalRegret);
					else
						for (a = 0; a < nbActions; a++)
							stratSum[a] += weight
									* (itStrat[a] = 1.0 / nbActions);
					itNextReal[player] *= itStrat[0];
					game.chosePlayerAction(0);
					continue;
				}
				game.back();
				nextDepth = depth--;
				node = iterNodes[depth];
				if (node.bType == 0) { // Chance
					if (depth > 0) {
						System.arraycopy(util[nextDepth], 0, util[depth], 0,
								nbPlayers);
						continue;
					}
					itUtil = util[nextDepth];
					break;
				}
				itUtil = util[depth];
				itNextUtil = util[nextDepth];
				// Player
				player = node.player;
				itStrat = strategies[depth];
				nbActions = (itRegret = node.regretSum).length;
				action = iterNodeLastAction[depth];
				for (p = 0; p < nbPlayers; p++)
					itUtil[p] += itStrat[action]
							* (pNodesUtil[depth][action][p] = itNextUtil[p]);
				itReal = realizationWeight[depth];
				if (nbActions - 1 == action) {
					weight = 1;
					for (p = 0; p < nbPlayers; p++)
						if (p != player)
							weight *= itReal[p];
					for (a = 0; a < nbActions; a++)
						itRegret[a] += weight
								* (pNodesUtil[depth][a][player] - itUtil[player]);
					if (updateVisits) {
						node.visits++;
						node.realWeightSum += weight;
					}
					if (lockPlayersNodes)
						node.unlock();
					if (depth > 0)
						continue;
					break;
				}
				action = ++iterNodeLastAction[depth];
				forward = true;
				System.arraycopy(itReal, 0,
						itNextReal = realizationWeight[nextDepth], 0, nbPlayers);
				itNextReal[player] *= itStrat[action];
				game.chosePlayerAction(action);
			}
			utilMgr.addIterUtil(itUtil);
		}
	}

	/**
	 * Gets the utility manager.
	 * 
	 * @return the utility manager
	 */
	public CSCFRMUtilityManager getUtilManager() {
		return utilMgr;
	}

	/**
	 * Gets the game.
	 * 
	 * @return the game
	 */
	public CSCFRMBaseGame getGame() {
		return game;
	}
}
