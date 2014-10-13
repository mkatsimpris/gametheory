/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for {@link CSCFRMGameBuilder}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CSCFRMGameBuilderValidator {

	/**
	 * Private constructor
	 */
	private CSCFRMGameBuilderValidator() {
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
	public static <PNode extends PlayerNode, Game extends CSCFRMGame<PNode>> boolean validateBuilder(
			CSCFRMGameBuilder<PNode, Game> builder,
			NodesProvider<PNode> provider, int nbIter) {
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(provider, "The nodes provider cannot be null");
		checkArgument(nbIter > 0,
				"The number of iterations to perform must be > 0");
		log.info("Validating game builder {}", builder);
		log.info("Validating base game");
		Game game = builder.getGame(provider);
		if (!CSCFRMGameValidator.validateAll(game, nbIter)) {
			log.error("Builder's base game validation failed");
			return false;
		}
		log.info("Validating sharing game");
		Game game2 = builder.getSharingGame(provider, game);
		if (!CSCFRMGameValidator.validateAll(game2, nbIter)) {
			log.error("Builder's sharing game validation failed");
			return false;
		}
		if (!IteratorsIdentityValidator.areTheSame(
				game.getPlayerNodesIterator(), game2.getPlayerNodesIterator())) {
			log.error("Base game and sharing game don't provide compatible player nodes iterators.");
			return false;
		}
		return true;
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
	public static <PNode extends PlayerNode, Game extends CSCFRMGame<PNode>> boolean validateBuilder(
			CSCFRMGameBuilder<PNode, Game> builder,
			CSCFRMTerminalUtilReader utilReader, NodesProvider<PNode> provider,
			int nbIter) {
		checkNotNull(builder, "The builder cannot be null");
		checkNotNull(provider, "The nodes provider cannot be null");
		checkArgument(nbIter > 0,
				"The number of iterations to perform must be > 0");
		checkNotNull(utilReader, "The utility reader cannot be null");
		log.info("Validating game builder {}", builder);
		log.info("Validating base game");
		Game game = builder.getGame(provider);
		if (!CSCFRMGameValidator.validateAll(game, utilReader, nbIter)) {
			log.error("Builder's base game validation failed");
			return false;
		}
		log.info("Validating sharing game");
		Game game2 = builder.getSharingGame(provider, game);
		if (!CSCFRMGameValidator.validateAll(game2, utilReader, nbIter)) {
			log.error("Builder's sharing game validation failed");
			return false;
		}
		if (!IteratorsIdentityValidator.areTheSame(
				game.getPlayerNodesIterator(), game2.getPlayerNodesIterator())) {
			log.error("Base game and sharing game don't provide compatible player nodes iterators.");
			return false;
		}
		return true;
	}

}
