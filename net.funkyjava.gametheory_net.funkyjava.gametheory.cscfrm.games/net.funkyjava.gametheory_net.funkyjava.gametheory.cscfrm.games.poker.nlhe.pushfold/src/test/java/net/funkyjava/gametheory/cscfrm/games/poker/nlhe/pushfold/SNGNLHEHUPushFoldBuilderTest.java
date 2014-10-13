package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static org.junit.Assert.assertTrue;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluatorProvider;

import org.junit.Test;

/**
 * Test class for {@link SNGNLHEHUPushFoldBuilder}
 * 
 * @author Pierre Mardon
 * 
 */
public class SNGNLHEHUPushFoldBuilderTest {

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testSNGNLHEHUPushFoldBuilder() throws Exception {
		assertTrue("The SNG builder is invalid", Valid.isValid(
				new SNGNLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), 5, 10, 100, 5),
				new DefaultNodesProvider(), 100000));
	}

}
