package net.funkyjava.gametheory.play;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import net.funkyjava.gametheory.commonmodel.game.GameChancePicker;
import net.funkyjava.gametheory.commonmodel.game.GameObserver;
import net.funkyjava.gametheory.commonmodel.game.GamePlayerDecider;
import net.funkyjava.gametheory.commonmodel.game.GameTreeWalker;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMFullGame;

/**
 * Contains all arguments to run a game.
 * 
 * @author Pierre Mardon
 * 
 * @see GameRunner
 */
public class GameRunnerArgs {

	/** The chance picker. */
	private final GameChancePicker chancePicker;

	/** The tree walker. */
	private final GameTreeWalker treeWalker;

	/** The deciders. */
	private final GamePlayerDecider[] deciders;

	/** The observers. */
	private final GameObserver[] observers;

	/**
	 * The default constructor.
	 * 
	 * @param chancePicker
	 *            the chance picker
	 * @param treeWalker
	 *            the tree walker
	 * @param deciders
	 *            the deciders
	 * @param observers
	 *            the observers
	 */
	public GameRunnerArgs(GameChancePicker chancePicker,
			GameTreeWalker treeWalker, GamePlayerDecider[] deciders,
			GameObserver... observers) {
		this.chancePicker = checkNotNull(chancePicker,
				"The chance picker cannot be null");
		this.treeWalker = checkNotNull(treeWalker,
				"The tree walker cannot be null");
		this.deciders = checkNotNull(deciders,
				"The deciders array cannot be null");
		checkArgument(deciders.length > 1,
				"Deciders must provide all players deciders (so at least two)");
		this.observers = checkNotNull(observers,
				"Observers array cannot be null");
	}
	/**
	 * Arguments to make a game the {@link GameChancePicker},
	 * {@link GameTreeWalker}, and all {@link GamePlayerDecider} for game
	 * simulation. It means that the game will handle all players decisions and
	 * chances draws. Can be useful to check if game simulations gives the
	 * computed utility of the game.
	 * 
	 * @param game
	 *            the game
	 */
	public GameRunnerArgs(CSCFRMFullGame<?> game) {
		this(game, game, getDecidersArrayFromOne(game, game.getNbPlayers()));
	}

	/**
	 * Arguments that gives games as deciders. The first one will be the
	 * {@link GameTreeWalker} and the {@link GameChancePicker}.
	 * 
	 * @param playersGames
	 *            the players games
	 */
	public GameRunnerArgs(CSCFRMFullGame<?>[] playersGames) {
		this(playersGames[0], playersGames[0], playersGames);
	}
	/**
	 * Gets the chance picker.
	 * 
	 * @return the chance picker
	 */
	public GameChancePicker getChancePicker() {
		return chancePicker;
	}

	/**
	 * Gets the tree walker.
	 * 
	 * @return the tree walker
	 */
	public GameTreeWalker getTreeWalker() {
		return treeWalker;
	}

	/**
	 * Gets the deciders.
	 * 
	 * @return the deciders
	 */
	public GamePlayerDecider[] getDeciders() {
		return deciders;
	}

	/**
	 * Gets the observers.
	 * 
	 * @return the observers
	 */
	public GameObserver[] getObservers() {
		return observers;
	}

	/**
	 * Gets the deciders array from one element.
	 * 
	 * @param decider
	 *            the decider
	 * @param nbPlayers
	 *            the number of players
	 * @return the deciders array
	 */
	private static GamePlayerDecider[] getDecidersArrayFromOne(
			GamePlayerDecider decider, int nbPlayers) {
		checkNotNull(decider, "The decider is null");
		checkArgument(nbPlayers > 1, "The number of players must be > 1");
		GamePlayerDecider[] res = (GamePlayerDecider[]) new Object[nbPlayers];
		for (int i = 0; i < nbPlayers; i++)
			res[i] = decider;
		return res;
	}
}
