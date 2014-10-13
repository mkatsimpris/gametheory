package net.funkyjava.gametheory.cscfrm.model.game;

import net.funkyjava.gametheory.commonmodel.game.GameChancePicker;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;

/**
 * Interface for state machine games that can be supplied to the CSCFRM
 * algorithm.
 * 
 * @author Pierre Mardon
 * 
 */
public interface CSCFRMBaseGame extends GameChancePicker {

	/**
	 * Called before starting an iteration.
	 */
	void onIterationStart();

	/**
	 * Walk the tree one step back.
	 */
	void back();

	/**
	 * Get current node.
	 * 
	 * @return node matching current game state
	 */
	Node getCurrentNode();

	/**
	 * Get number of players.
	 * 
	 * @return Number of players for this game
	 */
	int getNbPlayers();

	/**
	 * Get max game tree depth.
	 * 
	 * @return game tree depth
	 */
	int getMaxDepth();

	/**
	 * Get maximum number of player actions for each player nodes.
	 * 
	 * @return max number of player actions
	 */
	int getMaxNbPlActions();

	/**
	 * <p>
	 * Get game unique id. If the game is parameterized, this id should take it
	 * into account so that there can be no confusion. To keep coherence, it
	 * should also include a version number. This id is typically used to write
	 * persistent iteration data into files.
	 * </p>
	 * <p>
	 * Caution : this id identifies game implementation but should not be
	 * instance-dependent.
	 * </p>
	 * 
	 * @return the game unique id
	 */
	String getUId();
}
