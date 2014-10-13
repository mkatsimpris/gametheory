package net.funkyjava.gametheory.commonmodel.game;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode.Type;
/**
 * <p>
 * {@link GameChancePicker} can observe game's start and players action via the
 * {@link NoChanceGameObserver} methods, and should provide a coherent chance
 * draw every time a {@link Type#CHANCE} node is reached via its
 * {@link #choseChanceAction()} method.
 * </p>
 * <p>
 * A class implementing {@link GameObserver} and {@link GameChancePicker} should
 * expect calls on {@link #choseChanceAction()} EXCLUSIVE OR
 * {@link GameObserver#choseChanceAction(int)} depending on its role.
 * </p>
 * 
 * @author Pierre Mardon
 */
public interface GameChancePicker extends NoChanceGameObserver {

	/**
	 * Chose chance action.
	 * 
	 * @return the int
	 */
	int choseChanceAction();
}
