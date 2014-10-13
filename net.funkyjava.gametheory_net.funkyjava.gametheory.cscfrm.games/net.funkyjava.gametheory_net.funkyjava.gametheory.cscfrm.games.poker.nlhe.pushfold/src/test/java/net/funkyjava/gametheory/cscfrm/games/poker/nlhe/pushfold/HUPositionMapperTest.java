package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static org.junit.Assert.assertTrue;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;

import org.junit.Test;

/**
 * Test class for {@link HUPositionMapper}
 * 
 * @author Pierre Mardon
 * 
 */
public class HUPositionMapperTest {

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testHUPositionMapper() throws Exception {
		assertTrue("HU position mapper isn't valid",
				Valid.isValid(new HUPositionMapper(), 2, 50));
	}

}
