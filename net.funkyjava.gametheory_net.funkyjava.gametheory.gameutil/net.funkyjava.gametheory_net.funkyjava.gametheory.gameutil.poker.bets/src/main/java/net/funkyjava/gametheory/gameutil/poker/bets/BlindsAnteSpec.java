/**
 * 
 */
package net.funkyjava.gametheory.gameutil.poker.bets;

import lombok.Getter;
import lombok.experimental.Builder;

/**
 * @author Pierre Mardon
 * 
 */
@Builder
public class BlindsAnteSpec {

	@Getter
	private final boolean enableAnte, enableBlinds, isCash;

	@Getter
	private final int sbIndex, bbIndex, sbValue, bbValue, anteValue,
			firstPlayerAfterBlinds;

	@Getter
	private final boolean[] shouldPostEnteringBb;
	
}
