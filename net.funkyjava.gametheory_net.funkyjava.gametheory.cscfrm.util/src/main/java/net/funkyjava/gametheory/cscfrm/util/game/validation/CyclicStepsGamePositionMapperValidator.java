/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import static com.google.common.base.Preconditions.checkArgument;
import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Class providing static methods to validate
 * {@link CyclicStepsGamePositionMapper}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class CyclicStepsGamePositionMapperValidator {

	/**
	 * Private constructor
	 */
	private CyclicStepsGamePositionMapperValidator() {
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
	public static boolean validateAllStepsMapping(
			CyclicStepsGamePositionMapper mapper, int nbPlayers, int nbSteps) {
		checkArgument(nbPlayers > 1, "Provided number of players is <= 1");
		checkArgument(nbSteps > 0, "Provided number of step is < 1");
		try {
			for (int fromStep = 0; fromStep < nbSteps; fromStep++)
				for (int toStep = 0; toStep < nbSteps; toStep++) {
					if (!validateStepsMapping(mapper, nbPlayers, fromStep,
							toStep))
						return false;
				}
		} catch (Exception e) {
			log.error(
					"An exception was thrown when validating the position mapper.",
					e);
			return false;
		}
		return true;
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
	public static boolean validateStepsMapping(
			CyclicStepsGamePositionMapper mapper, int nbPlayers, int fromStep,
			int toStep) {
		if (nbPlayers <= 1) {
			log.error("Provided number of players is <= 1");
			return false;
		}
		boolean[] posCheck = new boolean[nbPlayers];
		for (int pos = 0; pos < nbPlayers; pos++) {
			int next = mapper.getNextPlayerPosition(fromStep, toStep, pos);
			if (next < 0 || next >= nbPlayers) {
				log.error("Failed validating position mapper, "
						+ "the next position {} for origin position {} "
						+ "from step {} to step {} must be >=0 and < {}", next,
						pos, fromStep, toStep, nbPlayers);
				return false;
			}
			if (posCheck[next]) {
				log.error("Failed validating position mapper, "
						+ "the next position {} for origin position {} "
						+ "from step {} to step {} was already attributed",
						next, pos, fromStep, toStep);
				return false;
			}
			posCheck[next] = true;
		}
		return true;

	}
}
