/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;

/**
 * @author Pierre Mardon
 * 
 */
public class SharedPot<Id> {

	@Getter
	private final List<PotShare<Id>> shares;
	@Getter
	private final Pot<Id> pot;

	private SharedPot(Pot<Id> pot, List<PotShare<Id>> shares) {
		this.pot = pot;
		this.shares = Collections.unmodifiableList(shares);
	}

	public static <Id> SharedPot<Id> sharePot(@NonNull Pot<Id> pot,
			@NonNull List<Id> winners, @NonNull Id oddChipsWinner) {
		checkArgument(winners.contains(oddChipsWinner),
				"List of winners must contain the odd chips winner");
		checkArgument(pot.getPlayers().containsAll(winners),
				"The winners didn't all contribute to the pot");
		List<PotShare<Id>> shares = new LinkedList<>();
		final int val = pot.getValue();
		int share = val / winners.size();
		int extra = val % winners.size();
		for (Id winner : winners)
			if (winner != oddChipsWinner)
				shares.add(new PotShare<>(share, winner));
		shares.add(new PotShare<>(share + extra, oddChipsWinner));
		return new SharedPot<Id>(pot, shares);
	}
}
