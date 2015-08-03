package net.funkyjava.gametheory.commonmodel.game;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode.Type;

/**
 * A {@link NoChanceGameObserver} can be notified of game's start and players
 * actions (when game walk reaches a {@link Type#PLAYER} node).
 * 
 * @author Pierre Mardon
 */
public interface NoChanceGameObserver {

	/**
	 * On game start.
	 */
	void onGameStart();

	/**
	 * A player action has been chosen.
	 * 
	 * @param actionIndex
	 *            the action index
	 */
	void onPlayerActionChosen(int actionIndex);
}
