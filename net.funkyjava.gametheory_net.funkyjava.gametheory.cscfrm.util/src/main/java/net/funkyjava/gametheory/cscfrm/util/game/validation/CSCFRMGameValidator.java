/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for {@link CSCFRMGame}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CSCFRMGameValidator {

	/**
	 * Private constructor
	 */
	private CSCFRMGameValidator() {
	}

	/**
	 * Validates a game's iterator by checking for null iterator and iterator's
	 * nodes and comparing two iterators enumerations.
	 * 
	 * @param <PNode>
	 *            the player node type of the game
	 * 
	 * @param game
	 *            the game to validate
	 * @return true when validations passed
	 */
	public static <PNode extends PlayerNode> boolean validateIterator(
			CSCFRMGame<PNode> game) {
		checkNotNull(game, "The game cannot be null");
		log.info("Validating iterator for game {}", game.getUId());
		return IteratorsIdentityValidator.areTheSame(
				game.getPlayerNodesIterator(), game.getPlayerNodesIterator());
	}

	/**
	 * Validate player nodes iterator and the game itself by simulating
	 * iterations. The utility reader provided will be tested with each
	 * encountered {@link TerminalNode} that has {@link Node#id} >= 0
	 * 
	 * @param <PNode>
	 *            the player nodes type
	 * @param game
	 *            the game to validate
	 * @param utilReader
	 *            the utility reader which provides utility pointed by terminal
	 *            nodes with {@link Node#id} >=0
	 * @param nbIter
	 *            the number of test iterations to perform
	 * @return true when every validations passed
	 */
	public static <PNode extends PlayerNode> boolean validateAll(
			CSCFRMGame<PNode> game, CSCFRMTerminalUtilReader utilReader,
			int nbIter) {
		return validateIterator(game)
				&& CSCFRMBaseGameValidator.validateForCSCFRM(game, utilReader,
						nbIter);
	}

	/**
	 * Validate player nodes iterator and the game itself by simulating
	 * iterations. All terminal nodes must provide direct payoffs.
	 * 
	 * @param <PNode>
	 *            the player nodes type
	 * @param game
	 *            the game to validate
	 * @param nbIter
	 *            the number of test iterations to perform
	 * @return true when every validations passed
	 */
	public static <PNode extends PlayerNode> boolean validateAll(
			CSCFRMGame<PNode> game, int nbIter) {
		return validateIterator(game)
				&& CSCFRMBaseGameValidator.validateForCSCFRM(game, nbIter);
	}
}
