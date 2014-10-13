/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo;

import static net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.Generator.handRanks;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.HoldemEvaluator;

/**
 * 2+2 hand evaluator.
 * 
 * @author Pierre Mardon
 * 
 */
public class TwoPlusTwoEvaluator implements HoldemEvaluator {

	private static final IntCardsSpec spec = new IntCardsSpec() {

		@Override
		public int getStandardRank(int card) {
			return (card - 1) / 4;
		}

		@Override
		public int getStandardColor(int card) {
			return (card - 1) % 4;
		}

		@Override
		public int getOffset() {
			return 1;
		}

		@Override
		public boolean sameColor(int card1, int card2) {
			return (card1 - 1) % 4 == (card2 - 1) % 4;
		}

		@Override
		public boolean sameRank(int card1, int card2) {
			return (card1 - 1) / 4 == (card2 - 1) / 4;
		}
	};

	/**
	 * The constructor. Will generate tables if not already done.
	 */
	public TwoPlusTwoEvaluator() {
		Generator.generateTables();
	}

	/**
	 * Compare two players hold'em hands
	 * 
	 * @param h1
	 *            the first player's hand
	 * @param h2
	 *            the second player's hand
	 * @return > 0 when first player wins, < 0 when second player wins, and 0 on
	 *         equality
	 */
	@Override
	public int compareHands(int[] h1, int[] h2, int[] board) {
		int b;
		return handRanks[handRanks[(b = handRanks[handRanks[handRanks[handRanks[handRanks[53 + board[0]]
				+ board[1]]
				+ board[2]]
				+ board[3]]
				+ board[4]])
				+ h1[0]]
				+ h1[1]]
				- handRanks[handRanks[b + h2[0]] + h2[1]];

	}

	@Override
	public int getEval(int[] hand) {
		return handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[handRanks[53 + hand[0]]
				+ hand[1]]
				+ hand[2]]
				+ hand[3]]
				+ hand[4]]
				+ hand[5]]
				+ hand[6]];
	}

	@Override
	public void getEvals(int[][] hands, int[] board, int[] dest) {
		int b = handRanks[handRanks[handRanks[handRanks[handRanks[53 + board[0]]
				+ board[1]]
				+ board[2]]
				+ board[3]]
				+ board[4]];
		for (int i = 0; i < hands.length; i++)
			dest[i] = handRanks[handRanks[b + hands[i][0]] + hands[i][1]];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.funkyjava.cscfrm.game.poker.he.handeval.itf.HoldemEvaluator#getCardsSpec
	 * ()
	 */
	@Override
	public IntCardsSpec getCardsSpec() {
		return spec;
	}

}
