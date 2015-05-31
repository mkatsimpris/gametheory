package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.AllArgsConstructor;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;

/**
 * A sample {@link NLBetRangeSlicer} to build a push/fold tree.
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class NLPushFoldBetRangeSlicer implements NLBetRangeSlicer {

	@Override
	public int[] slice(NLHandRounds hand) {
		BetChoice choice = hand.getBetChoice();
		if (!choice.exists()) {
			throw new IllegalArgumentException("There's no choice to slice");
		}
		if (choice.exists() && choice.getRaiseRange().exists()) {
			return new int[] { NLBetRangeSlicer.fold,
					choice.getRaiseRange().getMax() };
		}
		if (choice.exists() && choice.getBetRange().exists()) {
			return new int[] { NLBetRangeSlicer.fold,
					choice.getBetRange().getMax() };
		}
		if (choice.exists() && choice.getCallValue().isValid()) {
			if (choice.getCallValue().isCheck()) {
				return new int[choice.getCallValue().getValue()];
			}
			return new int[] { NLBetRangeSlicer.fold,
					choice.getCallValue().getValue() };
		}
		throw new IllegalArgumentException(
				"Invalid NLHand state for push/fold game");
	}
}
