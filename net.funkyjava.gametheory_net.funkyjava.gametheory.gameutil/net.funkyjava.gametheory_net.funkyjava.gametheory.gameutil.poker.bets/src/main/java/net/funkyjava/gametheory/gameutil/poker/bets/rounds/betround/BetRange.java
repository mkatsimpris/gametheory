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
public class BetRange {

	private int min;
	private int max;

	public boolean exists() {
		return min > 0 && max >= min;
	}

	public static BetRange getNoRange() {
		return new BetRange(-1, -1);
	}

	public static BetRange getSingleton(int singleValue) {
		return new BetRange(singleValue, singleValue);
	}

}
