/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.betround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class RaiseRange {

	private int bet;
	private int min;
	private int max;

	public boolean exists() {
		return min > 0 && max >= min;
	}

	public int getMinToAdd() {
		return min - bet;
	}

	public int getMaxToAdd() {
		return max - bet;
	}

	public static RaiseRange getNoRange() {
		return new RaiseRange(-1, -1, -1);
	}

	public static RaiseRange getSingleton(int bet, int singleValue) {
		return new RaiseRange(bet, singleValue, singleValue);
	}

}
