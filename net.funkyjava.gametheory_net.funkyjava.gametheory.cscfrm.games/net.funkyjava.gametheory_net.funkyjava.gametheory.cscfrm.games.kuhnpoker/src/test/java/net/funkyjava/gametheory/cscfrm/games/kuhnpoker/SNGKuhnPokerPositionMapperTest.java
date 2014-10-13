package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;

import static org.junit.Assert.assertTrue;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;

import org.junit.Test;

/**
 * Test class for {@link SNGKuhnPokerPositionMapper}
 * 
 * @author Pierre Mardon
 * 
 */
public class SNGKuhnPokerPositionMapperTest {

	/**
	 * Validate mapper
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testSNGKuhnPokerPositionMapper() throws Exception {
		assertTrue("", Valid.isValid(new SNGKuhnPokerPositionMapper(), 2, 20));
	}

}
