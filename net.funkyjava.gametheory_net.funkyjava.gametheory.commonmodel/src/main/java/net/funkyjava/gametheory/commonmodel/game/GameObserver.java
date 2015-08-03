package net.funkyjava.gametheory.commonmodel.game;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode.Type;

/**
 * A {@link GameObserver} can watch a full game : game's start and players
 * action via the {@link NoChanceGameObserver} methods and the chances actions
 * when reaching a {@link Type#CHANCE} node via
 * {@link #onChanceActionChosen(int)}.
 * 
 * @author Pierre Mardon
 */
public interface GameObserver extends NoChanceGameObserver {

	/**
	 * A chance action has been chosen.
	 * 
	 * @param actionIndex
	 *            the action index
	 */
	void onChanceActionChosen(int actionIndex);
}
