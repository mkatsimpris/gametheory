/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.IllegalComponentStateException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;

/**
 * Representation of the pot and all players that contributed to it
 * 
 * @author Pierre Mardon
 * @param <PlayerId>
 *            the players ids class
 * 
 */
public class Pot<PlayerId> {

	/**
	 * The pot's value
	 */
	@Getter
	private int value;

	/**
	 * Players that contributed to the pot
	 */
	@Getter
	private final List<PlayerId> players;

	/**
	 * Constructor
	 * 
	 * @param value
	 *            the value of the pot
	 * @param players
	 *            the contributing players
	 */
	public Pot(int value, List<PlayerId> players) {
		this.value = value;
		this.players = Collections.unmodifiableList(players);
	}

	private void setValue(int value) {
		this.value = value;
	}

	/**
	 * Get a copy of this pot for another representation of players ids
	 * 
	 * @param <Id>
	 *            the new players ids class
	 * @param players
	 *            the contributing players
	 * @return the new pot representation
	 */
	public <Id> Pot<Id> getCopy(List<Id> players) {
		checkArgument(
				this.players.size() == players.size(),
				"The new representation of contributing players has not the same number of members of the original list");
		return new Pot<Id>(value, players);
	}

	/**
	 * Check if this pot is an excedent bet
	 * 
	 * @return true when there is only one contributing player
	 */
	public boolean isExcedentBet() {
		return players.size() == 1;
	}

	/**
	 * Create pots with players bets and in-hand data for players ids integer
	 * index representation
	 * 
	 * @param bets
	 *            the indexed bets
	 * @param inHand
	 *            the indexed in-hand state
	 * @return the list of pots
	 */
	public static List<Pot<Integer>> getPots(int[] bets, boolean[] inHand) {
		int nbPlayers = bets.length;
		checkArgument(nbPlayers == inHand.length,
				"Arrays must have the same size");
		checkArgument(nbPlayers > 1, "There must be at least two players...");
		List<Pot<Integer>> res = new LinkedList<>();
		int[] newBets = bets.clone();
		while (true) {
			List<Integer> players = new LinkedList<>();
			int minBet = Integer.MAX_VALUE;
			for (int p = 0; p < nbPlayers; p++) {
				if (inHand[p] && newBets[p] > 0) {
					players.add(p);
					minBet = Math.min(minBet, newBets[p]);
				}
			}
			if (minBet == 0 || minBet == Integer.MAX_VALUE) {
				return res;
			}
			if (minBet < 0)
				throw new IllegalComponentStateException(
						"Min bet < 0 in pots loop");
			int value = 0;
			int tmp;
			for (int p = 0; p < nbPlayers; p++) {
				tmp = Math.min(minBet, newBets[p]);
				if (tmp == 0)
					continue;
				value += tmp;
				newBets[p] -= tmp;
			}
			res.add(new Pot<Integer>(value, players));
		}
	}

	/**
	 * Create pots with players bets and in-hand data for players ids integer
	 * index representation, based on previously created pots
	 * 
	 * @param lastPot
	 *            the previously created pots
	 * @param bets
	 *            the indexed bets
	 * @param inHand
	 *            the indexed in-hand state
	 * @return the list of pots
	 */
	public static List<Pot<Integer>> getPots(Pot<Integer> lastPot, int[] bets,
			boolean[] inHand) {
		int nbPlayers = bets.length;
		checkArgument(nbPlayers == inHand.length,
				"Arrays must have the same size");
		checkArgument(nbPlayers > 1, "There must be at least two players...");
		List<Pot<Integer>> res = new LinkedList<>();
		int[] newBets = bets.clone();
		while (true) {
			List<Integer> players = new LinkedList<>();
			int minBet = Integer.MAX_VALUE;
			for (int p = 0; p < nbPlayers; p++) {
				if (inHand[p] && newBets[p] > 0) {
					players.add(p);
					minBet = Math.min(minBet, newBets[p]);
				}
			}
			if (minBet == 0 || minBet == Integer.MAX_VALUE) {
				return res;
			}
			if (minBet < 0)
				throw new IllegalComponentStateException(
						"Min bet < 0 in pots loop");
			int value = 0;
			int tmp;
			for (int p = 0; p < nbPlayers; p++) {
				tmp = Math.min(minBet, newBets[p]);
				value += tmp;
				newBets[p] -= tmp;
			}
			if (res.isEmpty() && players.size() == lastPot.getPlayers().size()
					&& players.containsAll(lastPot.getPlayers()))
				lastPot.setValue(lastPot.getValue() + value);
			else
				res.add(new Pot<Integer>(value, players));
		}
	}

	@Override
	public String toString() {
		return value + " - " + Arrays.toString(players.toArray());
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Pot<?>))
			return false;
		Pot<?> p = (Pot<?>) o;
		if (p.value != value || p.players.size() != players.size())
			return false;
		for (PlayerId id : players)
			if (!p.players.contains(id))
				return false;
		return true;
	}
}
