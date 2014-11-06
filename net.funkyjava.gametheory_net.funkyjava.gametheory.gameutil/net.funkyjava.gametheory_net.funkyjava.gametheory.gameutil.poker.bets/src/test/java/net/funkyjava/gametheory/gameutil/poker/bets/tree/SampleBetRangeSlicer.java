package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import java.awt.IllegalComponentStateException;
import java.util.List;

import lombok.AllArgsConstructor;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround.BetChoice;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.PlayersData;

/**
 * A sample {@link BetRangeSlicer}. Don't use it for real computing purpose.
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class SampleBetRangeSlicer implements BetRangeSlicer {

	/**
	 * After this, just check or fold and push
	 */
	private final int maxRaiseIndex;
	private final int maxNbMoves;

	@Override
	public int[] slice(List<Pot<Integer>> pots, PlayersData data,
			BetChoice choice, int raiseIndex, int betRoundIndex) {
		if (raiseIndex > maxRaiseIndex
				|| (!choice.getBetRange().exists() && !choice.getRaiseRange()
						.exists())) {
			if (choice.getCallValue().isCheck()) {
				if (choice.getBetRange().exists())
					return new int[] { 0, choice.getBetRange().getMax() };
				if (choice.getRaiseRange().exists())
					return new int[] { 0, choice.getRaiseRange().getMax() };
				throw new IllegalComponentStateException(
						"Can check but not bet or raise ?");
			}
			if (choice.getBetRange().exists())
				return new int[] { BetRangeSlicer.fold,
						choice.getCallValue().getValue(),
						choice.getBetRange().getMax() };
			if (choice.getRaiseRange().exists())
				return new int[] { BetRangeSlicer.fold,
						choice.getCallValue().getValue(),
						choice.getRaiseRange().getMax() };
			return new int[] { BetRangeSlicer.fold,
					choice.getCallValue().getValue() };
		}
		int nextMoveIndex = 0;
		final int[] res = new int[maxNbMoves];
		if (choice.getCallValue().isCheck())
			res[nextMoveIndex++] = choice.getCallValue().getValue();
		else {
			res[nextMoveIndex++] = BetRangeSlicer.fold;
			res[nextMoveIndex++] = choice.getCallValue().getValue();
		}
		int slice;
		int min, max;
		int maxBet = 0;
		int allPots = 0;
		for (Pot<Integer> p : pots) {
			allPots += p.getValue();
		}
		for (int i = 0; i < data.getBets().length; i++) {
			allPots += data.getBets()[i];
			if (data.getBets()[i] > maxBet)
				maxBet = data.getBets()[i];
		}
		if (choice.getBetRange().exists()) {
			min = Math.max(
					Math.max(choice.getBetRange().getMin(), allPots / 3),
					(int) (maxBet * 1.7));
			min = Math.min(min, choice.getBetRange().getMax());
			slice = ((max = choice.getBetRange().getMax()) - min)
					/ (maxNbMoves - nextMoveIndex);

		} else if (choice.getRaiseRange().exists()) {
			min = Math.max(
					Math.max(choice.getRaiseRange().getMin(), allPots / 3),
					(int) (maxBet * 1.7));
			min = Math.min(min, choice.getRaiseRange().getMax());
			slice = ((max = choice.getRaiseRange().getMax()) - min)
					/ (maxNbMoves - nextMoveIndex);
		} else
			throw new IllegalComponentStateException(
					"Inconsistant : cannot bet or raise");
		if (slice == 0) {
			res[nextMoveIndex++] = max;
			final int[] newRes = new int[nextMoveIndex];
			System.arraycopy(res, 0, newRes, 0, nextMoveIndex);
			return newRes;
		}
		for (int i = 0; i < res.length - nextMoveIndex - 1; i++) {
			res[nextMoveIndex + i] = min + i * slice;
		}
		res[res.length - 1] = max;
		return res;
	}
}
