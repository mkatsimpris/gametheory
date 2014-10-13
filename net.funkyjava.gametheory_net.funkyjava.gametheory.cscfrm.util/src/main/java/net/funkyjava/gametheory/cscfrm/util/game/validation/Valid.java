/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import java.util.Iterator;

import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMBaseGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * Agglomeration class for validators
 * 
 * @author Pierre Mardon
 * 
 */
public class Valid {

	/**
	 * Private constructor
	 */
	private Valid() {
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
	public static boolean isValid(CSCFRMBaseGame game, int nbIter) {
		return CSCFRMBaseGameValidator.validateForCSCFRM(game, nbIter);
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
	public static boolean isValid(CSCFRMBaseGame game,
			CSCFRMTerminalUtilReader utilReader, int nbIter) {
		return CSCFRMBaseGameValidator.validateForCSCFRM(game, utilReader,
				nbIter);
	}

	/**
	 * Validates a builder.
	 * 
	 * @param <PNode>
	 *            the player node type
	 * 
	 * @param <Game>
	 *            the game type
	 * @param builder
	 *            the builder to test
	 * @param provider
	 *            the nodes provider
	 * @param nbIter
	 *            the number of iterations to perform validation
	 * @return true when all validations passed
	 */
	public static <PNode extends PlayerNode, Game extends CSCFRMGame<PNode>> boolean isValid(
			CSCFRMGameBuilder<PNode, Game> builder,
			NodesProvider<PNode> provider, int nbIter) {
		return CSCFRMGameBuilderValidator.validateBuilder(builder, provider,
				nbIter);
	}

	/**
	 * Validates a builder.
	 * 
	 * @param <PNode>
	 *            the player node type
	 * 
	 * @param <Game>
	 *            the game type
	 * @param builder
	 *            the builder to test
	 * @param utilReader
	 *            the terminal utility reader
	 * @param provider
	 *            the nodes provider
	 * @param nbIter
	 *            the number of iterations to perform validation
	 * @return true when all validations passed
	 */
	public static <PNode extends PlayerNode, Game extends CSCFRMGame<PNode>> boolean isValid(
			CSCFRMGameBuilder<PNode, Game> builder,
			CSCFRMTerminalUtilReader utilReader, NodesProvider<PNode> provider,
			int nbIter) {
		return CSCFRMGameBuilderValidator.validateBuilder(builder, utilReader,
				provider, nbIter);
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
	public static <PNode extends PlayerNode> boolean isValid(
			CSCFRMGame<PNode> game, int nbIter) {
		return CSCFRMGameValidator.validateAll(game, nbIter);
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
	public static <PNode extends PlayerNode> boolean isValid(
			CSCFRMGame<PNode> game, CSCFRMTerminalUtilReader utilReader,
			int nbIter) {
		return CSCFRMGameValidator.validateAll(game, utilReader, nbIter);
	}

	/**
	 * Ensures that two iterators iterate over the same set and in the same
	 * order.
	 * 
	 * @param <P>
	 *            the class of the objects to iterate on
	 * @param it
	 *            the first iterator
	 * @param it2
	 *            the second iterator
	 * @return true when all objects are the same and in the same order
	 */
	public static <P> boolean areTheSame(Iterator<P> it, Iterator<P> it2) {
		return IteratorsIdentityValidator.areTheSame(it, it2);
	}

	/**
	 * Validate all steps position mapping, assuming that the target game
	 * provides position mapping between each possible couple of steps.
	 * 
	 * @param nbPlayers
	 *            the number of players
	 * @param nbSteps
	 *            the number of steps
	 * @param mapper
	 *            the mapper
	 * @return true when valid
	 */
	public static boolean isValid(CyclicStepsGamePositionMapper mapper,
			int nbPlayers, int nbSteps) {
		return CyclicStepsGamePositionMapperValidator.validateAllStepsMapping(
				mapper, nbPlayers, nbSteps);
	}

	/**
	 * Validate position mapping from one origin step to one target step
	 * 
	 * @param nbPlayers
	 *            the number of players
	 * @param mapper
	 *            the mapper
	 * @param fromStep
	 *            origin step
	 * @param toStep
	 *            target step
	 * @return true when valid
	 */
	public static boolean isValid(CyclicStepsGamePositionMapper mapper,
			int nbPlayers, int fromStep, int toStep) {
		return CyclicStepsGamePositionMapperValidator.validateStepsMapping(
				mapper, nbPlayers, fromStep, toStep);
	}

	/**
	 * Validates all steps builders and ensures that tested terminal nodes id
	 * point to an actual step.
	 * 
	 * @param <PNode>
	 *            the player node type
	 * @param <StepGameClass>
	 *            the step game class
	 * @param builder
	 *            the builder
	 * @param provider
	 *            the nodes provider
	 * @param nbIter
	 *            the number of iterations to execute on each step game
	 * @return true when all validations passed
	 */
	public static <PNode extends PlayerNode, StepGameClass extends CSCFRMGame<PNode>> boolean isValid(
			CSCFRMCyclicStepsGameBuilder<PNode, StepGameClass> builder,
			NodesProvider<PNode> provider, int nbIter) {
		return CSCFRMCyclicStepsGameBuilderValidator.validate(builder,
				provider, nbIter);
	}
}
