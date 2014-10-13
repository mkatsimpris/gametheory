/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets.pots;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class PotShare<Id> {
	@Getter
	private final int value;
	@Getter
	private final Id player;

}
