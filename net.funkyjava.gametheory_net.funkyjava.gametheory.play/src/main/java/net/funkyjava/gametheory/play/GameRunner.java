package net.funkyjava.gametheory.play;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.LinkedList;
import java.util.List;

import net.funkyjava.gametheory.commonmodel.game.GameChancePicker;
import net.funkyjava.gametheory.commonmodel.game.GameObserver;
import net.funkyjava.gametheory.commonmodel.game.GamePlayerDecider;
import net.funkyjava.gametheory.commonmodel.game.GameTreeWalker;
import net.funkyjava.gametheory.commonmodel.game.NoChanceGameObserver;
import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;
import lombok.extern.slf4j.Slf4j;

/**
 * A game runner is meant to play games given several objects :
 * <ul>
 * <li>
 * A {@link GameTreeWalker} that will be notified of all actions and provides
 * the current node at any time.</li>
 * <li>
 * A {@link GameChancePicker} that will be notified of all player actions and
 * will provide chances draws.</li>
 * <li>
 * Several {@link GamePlayerDecider} whose indexes are the players indexes, that
 * will be notified of other players actions and chances draws, and that should
 * provide player decisions for its index player.</li>
 * </ul>
 * 
 * <p>
 * Roles are not strict, eg the {@link GameChancePicker} can also be the
 * {@link GameTreeWalker}, several {@link GamePlayerDecider}, or a
 * {@link GameObserver}
 * </p>
 * <p>
 * If the {@link GameChancePicker} is a {@link GameObserver}, its
 * {@link GameObserver#onChanceActionChosen(int)} will never be called because it's
 * him who generates the chances draws.
 * </p>
 * <p>
 * If any {@link GameObserver} is a {@link GamePlayerDecider}, its
 * {@link GameObserver#onPlayerActionChosen(int)} will never be called for the
 * players he's deciding for.
 * </p>
 * <p>
 * So when a decider picks a player action (its
 * {@link GamePlayerDecider#onPlayerActionChosen(int)} is called),
 * {@link NoChanceGameObserver#onPlayerActionChosen(int)} will be called strictly
 * once on all other objects but him, even if he has several roles.
 * </p>
 * <p>
 * And when the chance picker chooses a chance draw (its
 * {@link GameChancePicker#choseChanceAction()} is called),
 * {@link GameObserver#onChanceActionChosen(int)} will be called strictly once on
 * all other objects but him, even if he has several roles.
 * </p>
 * 
 * @author Pierre Mardon
 * 
 */

@Slf4j
public class GameRunner {

	/** The chance picker. */
	private final GameChancePicker chancePicker;

	/** The tree walker. */
	private final GameTreeWalker treeWalker;

	/** The deciders. */
	private final GamePlayerDecider[] deciders;

	/** The players observers. */
	private final NoChanceGameObserver[] playersObservers;

	/** The chance observers. */
	private final GameObserver[] chanceObservers;

	/** The numbers... */
	private final int nbPlayers, nbPlObservers, nbChObservers;

	/** The payoffs array. */
	private double[] payoffs;

	/**
	 * The Constructor.
	 * 
	 * @param args
	 *            the arguments
	 * @see GameRunnerArgs
	 */
	public GameRunner(GameRunnerArgs args) {
		checkNotNull(args, "The game runner argument cannot be null");
		this.chancePicker = args.getChancePicker();
		this.treeWalker = args.getTreeWalker();
		this.deciders = args.getDeciders();
		this.nbPlayers = deciders.length;
		List<NoChanceGameObserver> allObservers = new LinkedList<>();
		allObservers.add(chancePicker);
		List<NoChanceGameObserver> playersObservers = new LinkedList<>();
		playersObservers.add(chancePicker);
		GameObserver[] observers = args.getObservers();
		if (!playersObservers.contains(treeWalker))
			playersObservers.add(treeWalker);
		for (int i = 0; i < nbPlayers; i++)
			if (!playersObservers.contains(deciders[i]))
				playersObservers.add(deciders[i]);
		if (observers != null)
			for (int i = 0; i < observers.length; i++)
				if (!playersObservers.contains(observers[i]))
					playersObservers.add(observers[i]);
		this.playersObservers = playersObservers
				.toArray(new NoChanceGameObserver[nbPlObservers = playersObservers
						.size()]);
		List<GameObserver> chanceObservers = new LinkedList<>();
		chanceObservers.add(treeWalker);
		for (int i = 0; i < nbPlayers; i++)
			if (!chanceObservers.contains(deciders[i]))
				chanceObservers.add(deciders[i]);
		if (observers != null)
			for (int i = 0; i < observers.length; i++)
				if (!chanceObservers.contains(observers[i]))
					chanceObservers.add(observers[i]);
		if (chanceObservers.contains(chancePicker))
			chanceObservers.remove(chancePicker);
		this.chanceObservers = chanceObservers
				.toArray(new GameObserver[nbChObservers = chanceObservers
						.size()]);
		log.info("GameRunner initialized for {} players", nbPlayers);
	}

	/**
	 * Run one game.
	 * 
	 * @param utilDest
	 *            the utility destination. If the terminal state provides
	 *            payoffs, they will be copied in this array.
	 * @return the terminal node id (can be < 0).
	 */
	public int runOneGame(final double[] utilDest) {
		MinPublicNode n;
		GamePlayerDecider plDec;
		int v;
		int i;
		for (i = 0; i < nbPlObservers; i++)
			playersObservers[i].onGameStart();
		while (true) {
			n = treeWalker.getCurrentNode();
			switch (n.getType()) {
			case CHANCE:
				v = chancePicker.choseChanceAction();
				for (i = 0; i < nbChObservers; i++)
					chanceObservers[i].onChanceActionChosen(v);
				continue;
			case PLAYER:
				v = (plDec = deciders[n.getPlayer()]).chosePlayerAction();
				for (i = 0; i < nbPlObservers; i++)
					if (playersObservers[i] != plDec)
						playersObservers[i].onPlayerActionChosen(v);
				continue;
			case TERMINAL:
				if ((payoffs = n.getPayoffs()) != null)
					System.arraycopy(payoffs, 0, utilDest, 0, nbPlayers);
				return n.getId();
			}
		}
	}

	/**
	 * Run one game specifying that a permutation over deciders must be applied.
	 * 
	 * @param utilDest
	 *            the utility destination. If the terminal state provides
	 *            payoffs, they will be copied in this array. The permutation is
	 *            not applied to those payoffs.
	 * @param decidersPerm
	 *            the deciders permutation.
	 * @return the terminal node id (can be < 0).
	 */
	public int runOneGame(final double[] utilDest, final int[] decidersPerm) {
		MinPublicNode n;
		GamePlayerDecider plDec;
		int v;
		int i;
		for (i = 0; i < nbPlObservers; i++)
			playersObservers[i].onGameStart();
		while (true) {
			n = treeWalker.getCurrentNode();
			switch (n.getType()) {
			case CHANCE:
				v = chancePicker.choseChanceAction();
				for (i = 0; i < nbChObservers; i++)
					chanceObservers[i].onChanceActionChosen(v);
				continue;
			case PLAYER:
				v = (plDec = deciders[decidersPerm[n.getPlayer()]])
						.chosePlayerAction();
				for (i = 0; i < nbPlObservers; i++)
					if (playersObservers[i] != plDec)
						playersObservers[i].onPlayerActionChosen(v);
				continue;
			case TERMINAL:
				if ((payoffs = n.getPayoffs()) != null)
					System.arraycopy(payoffs, 0, utilDest, 0, nbPlayers);
				return n.getId();
			}
		}
	}
}
