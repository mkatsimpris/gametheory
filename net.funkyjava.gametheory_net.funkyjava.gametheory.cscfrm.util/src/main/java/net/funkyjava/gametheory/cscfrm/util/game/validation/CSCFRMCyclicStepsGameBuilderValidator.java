/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.cscfrm.core.engine.CSCFRMTerminalUtilReader;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.cyclic.CSCFRMCyclicStepsGameBuilder;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for {@link CSCFRMCyclicStepsGameBuilder}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CSCFRMCyclicStepsGameBuilderValidator {

	/**
	 * Private constructor
	 */
	private CSCFRMCyclicStepsGameBuilderValidator() {
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
	public static <PNode extends PlayerNode, StepGameClass extends CSCFRMGame<PNode>> boolean validate(
			CSCFRMCyclicStepsGameBuilder<PNode, StepGameClass> builder,
			NodesProvider<PNode> provider, int nbIter) {
		checkNotNull(builder, "Builder cannot be null");
		checkNotNull(provider, "Nodes provider cannot be null");
		checkArgument(nbIter > 0,
				"The number of iterations to perform must be > 0");
		log.info("Validating cyclic steps game builder {}", builder.getUId());
		final int nbSteps = builder.getBuilders().size();
		CSCFRMTerminalUtilReader utilMock = new CSCFRMTerminalUtilReader() {

			@Override
			public void read(int id, double[] dest) throws InterruptedException {
				checkArgument(id >= 0 && id < nbSteps,
						"Unknown step for id %s", id);

			}
		};
		int i = 0;
		for (CSCFRMGameBuilder<PNode, StepGameClass> b : builder.getBuilders()) {
			if (!Valid.isValid(b, utilMock, provider, nbIter)) {
				log.error("Step {} builder is invalid", i);
				return false;
			}
			i++;
		}
		return true;
	}
}
