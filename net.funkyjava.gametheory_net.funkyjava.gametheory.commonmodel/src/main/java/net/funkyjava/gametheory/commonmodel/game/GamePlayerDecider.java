package net.funkyjava.gametheory.commonmodel.game;

import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode.Type;

/**
 * A {@link GamePlayerDecider} has the ability to chose an action for the
 * current {@link Type#PLAYER} node. When he has to, its
 * {@link #onPlayerActionChosen(int)} method will not be called. Its ability is
 * context-dependent, as a {@link GamePlayerDecider} can be supplied only to
 * play for one of the game's players.
 * 
 * @author Pierre Mardon
 */
public interface GamePlayerDecider extends GameObserver {

	/**
	 * Chose player action.
	 * 
	 * @return the index of the chosen player action
	 */
	int chosePlayerAction();
}
