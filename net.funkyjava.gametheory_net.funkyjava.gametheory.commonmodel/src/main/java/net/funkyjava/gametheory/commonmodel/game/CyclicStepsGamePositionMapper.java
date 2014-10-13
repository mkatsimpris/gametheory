package net.funkyjava.gametheory.commonmodel.game;

/**
 * Considering that a step game is a game that can be played using a
 * {@link GameTreeWalker}, a {@link GameChancePicker} and some
 * {@link GamePlayerDecider}, we define a cyclic steps game as a fixed set of
 * step games whose terminal states can refer to one of those step games.
 * Between steps, players may have to change places for the next one. For this
 * purpose, a mapper is needed.
 * 
 * @author Pierre Mardon
 * 
 * @see <a href="http://www.youtube.com/watch?v=8tYXfssLOSM">this</a>
 */
public interface CyclicStepsGamePositionMapper {

	/**
	 * Gets the next player position.
	 * 
	 * @param fromStep
	 *            the step id of the source step game
	 * @param toStep
	 *            the step id of the destination step game
	 * @param fromPos
	 *            the source position of the player
	 * @return the player position in destination step game
	 */
	int getNextPlayerPosition(int fromStep, int toStep, int fromPos);
}
