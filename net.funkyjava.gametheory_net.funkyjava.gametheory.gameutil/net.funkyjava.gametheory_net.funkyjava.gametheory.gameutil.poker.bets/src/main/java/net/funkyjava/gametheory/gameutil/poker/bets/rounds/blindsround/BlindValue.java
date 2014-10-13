/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.rounds.blindsround;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Pierre Mardon
 * 
 */
@Data
@AllArgsConstructor
public class BlindValue {

	public static enum Type {
		SB, BB
	}

	private Type type;
	private int value;

}
