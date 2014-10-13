/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode.Type;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMBaseGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for {@link CSCFRMBaseGame}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CSCFRMBaseGameValidator {

	/**
	 * Private constructor
	 */
	private CSCFRMBaseGameValidator() {
	}

	/**
	 * 
	 * Verifies game specifications, and that the state machine seems available
	 * to CSCFRM, Monte-Carlo way. Payoffs must be directly accessible in
	 * terminal nodes, {@link Node#id} will not be checked via a
	 * {@link CSCFRMTerminalUtilReader}.
	 * 
	 * @param game
	 *            the game to validate
	 * @param nbIter
	 *            the number of iterations to perform to test the game
	 * @return true when the game seems valid
	 */
	public static boolean validateForCSCFRM(CSCFRMBaseGame game, int nbIter) {
		return validateForCSCFRM(game, nbIter, null);
	}

	/**
	 * 
	 * Verifies game specifications, and that the state machine seems available
	 * to CSCFRM, Monte-Carlo way. Payoffs must be directly accessible in
	 * terminal nodes, or via the {@link CSCFRMTerminalUtilReader}, using the
	 * {@link Node#id} attribute. Any
	 * 
	 * @param game
	 *            the game to validate
	 * @param utilReader
	 *            utility reader for terminal nodes
	 * @param nbIter
	 *            the number of iterations to perform to test the game
	 * @return true when the game seems valid
	 */
	public static boolean validateForCSCFRM(CSCFRMBaseGame game,
			CSCFRMTerminalUtilReader utilReader, int nbIter) {
		checkNotNull(utilReader, "This method requires a not-null %s",
				CSCFRMTerminalUtilReader.class.getName());
		return validateForCSCFRM(game, nbIter, utilReader);
	}

	/**
	 * 
	 * Verifies game specifications, and that the state machine seems available
	 * to CSCFRM, Monte-Carlo way. When a not null
	 * {@link CSCFRMTerminalUtilReader} is provided, terminal nodes that provide
	 * a positive id will be read in it.
	 * 
	 * @param game
	 *            the game to validate
	 * @param nbIter
	 *            the number of iterations to perform to test the game
	 * @param utilReader
	 *            utility reader for terminal nodes.
	 * @return true when the game seems valid
	 */
	private static boolean validateForCSCFRM(CSCFRMBaseGame game, int nbIter,
			CSCFRMTerminalUtilReader utilReader) {
		checkNotNull(game, "Game to validate is null");
		checkArgument(nbIter > 0,
				"Incorrect number of iterations to validate game %s : %s",
				game.getUId(), nbIter);
		try {
			int maxDepth = game.getMaxDepth();
			if (maxDepth <= 0) {
				log.error(
						"Validation fails : game says its maximum depth is {}",
						maxDepth);
				return false;
			}
			int nbPlayers = game.getNbPlayers();
			if (nbPlayers <= 1) {
				log.error(
						"Validation fails : game says its number of players is {}",
						nbPlayers);
				return false;
			}
			log.info("Starting validation iterations on game {}", game.getUId());
			int maxPlAct = game.getMaxNbPlActions();
			IterResult res = new IterResult();
			for (int itCount = 0; itCount < nbIter; itCount++) {
				validateIteration(game, maxPlAct, nbPlayers, maxDepth, res,
						utilReader);
				if (!res.valid)
					return false;
			}
			return true;
		} catch (Exception e) {
			log.error("Validation failed on an exception", e);
			return false;
		}
	}

	private static class IterResult {
		public boolean valid = true;
		public boolean sawChance = false;
	}

	private static void validateIteration(CSCFRMBaseGame game, int maxPlAct,
			int nbPlayers, int maxDepth, IterResult res,
			CSCFRMTerminalUtilReader utilReader) throws Exception {
		int[] lastPlAct = new int[maxDepth];
		Node[] nodes = new Node[maxDepth];
		final double[] util = new double[nbPlayers];
		Node n;
		int depth = 0;
		game.onIterationStart();
		boolean forward = true;

		final byte chanceB = Type.CHANCE.byteType;
		final byte plB = Type.PLAYER.byteType;
		final byte termB = Type.TERMINAL.byteType;
		while (true) {
			if (depth >= maxDepth) {
				log.error(
						"Reached depth index {}, so maximum depth should be at least {}",
						depth, maxDepth + 1);
				res.valid = false;
				return;
			}
			if (forward) {
				n = nodes[depth] = game.getCurrentNode();
				if (nodes[depth] == null) {
					log.error("Validation failed : game provided a null node");
					res.valid = false;
					return;
				}
				if (n.bType == chanceB) {
					if (!res.sawChance) {
						res.sawChance = true;
						log.warn("Game provides a chance node. "
								+ "For CSCFRM performance, it should be avoided."
								+ " Chances should be computed in onIterationStart call.");
					}
					game.choseChanceAction();
					depth++;
					continue;
				}
				if (n.bType == termB) {
					if (n.payoffs == null && n.id < 0) {
						log.error("Validation failed : terminal node provides"
								+ " a null payoffs array and a negative id.");
						res.valid = false;
						return;
					}
					if (n.payoffs == null && utilReader == null) {
						log.error("Validation failed : terminal node provides"
								+ " a null payoffs array no utility reader was provided.");
						res.valid = false;
						return;
					}
					if (utilReader != null && n.id >= 0)
						utilReader.read(n.id, util);
					if (n.payoffs != null && n.payoffs.length != nbPlayers) {
						log.error(
								"Validation failed : provided payoffs array length"
										+ " is {}, expected number of players {}.",
								n.payoffs.length, nbPlayers);
						res.valid = false;
						return;
					}
					if (depth == 0)
						return;
					forward = false;
					depth--;
					continue;
				}
				if (n.bType == plB) {
					if (maxPlAct < 2) {
						log.error(
								"Provided max players action is {} and should be > 2 as"
										+ " there are player nodes", maxPlAct);
						res.valid = false;
						return;
					}
					if (n.regretSum == null) {
						log.error("Player node regret sum array is null");
						res.valid = false;
						return;
					}
					if (n.stratSum == null) {
						log.error("Player node strategy sum array is null");
						res.valid = false;
						return;
					}
					if (n.regretSum.length > maxPlAct) {
						log.error(
								"Player node regret sum array has a length {} "
										+ "superior to declared maximum players actions {}",
								maxPlAct);
						res.valid = false;
						return;
					}
					if (n.regretSum.length != n.stratSum.length) {
						log.error("Player node regret sum length {} differs "
								+ "from strategy sum length {}",
								n.regretSum.length, n.stratSum.length);
						res.valid = false;
						return;
					}
					if (n.player < 0) {
						log.error("Player node has a negative player index {}",
								n.player);
						res.valid = false;
						return;
					}
					if (n.player >= nbPlayers) {
						log.error(
								"Player node has a too high player index {}, "
										+ "expected < number of players = {}",
								n.player, nbPlayers);
						res.valid = false;
						return;
					}
					lastPlAct[depth] = 0;
					depth++;
					game.chosePlayerAction(0);
					continue;
				}
				log.error("Unknown node byte type {}", n.bType);
				res.valid = false;
				return;
			}
			game.back();
			n = game.getCurrentNode();
			if (n != nodes[depth]) {
				log.error("Node {} provided when coming back to depth index {}"
						+ " from {} differs from expected one {}", n, depth,
						depth + 1, nodes[depth]);
				res.valid = false;
				return;
			}
			if (n.bType == plB) {
				if (lastPlAct[depth] == n.regretSum.length - 1) {
					if (depth == 0)
						return;
					depth--;
					continue;
				}
				forward = true;
				game.chosePlayerAction(++lastPlAct[depth]);
				depth++;
				continue;
			}
			if (n.bType == chanceB) {
				if (depth == 0)
					return;
				depth--;
				continue;
			}
			log.error("Ooops, this shouldn't really happen : back at depth {},"
					+ " the current node is not a player node or a "
					+ "chance node... Internal error !", depth);
			throw new Exception("INTERNAL ERROR WHEN VALIDATING");
		}
	}
}
