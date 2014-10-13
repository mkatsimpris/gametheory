package net.funkyjava.gametheory.cscfrm.games.poker.nlhe.pushfold;

import static org.junit.Assert.assertTrue;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluatorProvider;

import org.junit.Test;

/**
 * @author Pierre Mardon
 * 
 */
public class NLHEHUPushFoldBuilderTest {

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testNLHEHUPushFoldBuilderHoldemEvaluatorProviderIntIntIntIntInt()
			throws Exception {
		int nbIter = 1000000;
		assertTrue("Not SNG 10 bb game is invalid", Valid.isValid(
				new NLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), 5, 10, 200, 200),
				new DefaultNodesProvider(), nbIter));

		assertTrue("Not SNG with immediate all-in game is invalid",
				Valid.isValid(new NLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), 5, 10, 5, 200),
						new DefaultNodesProvider(), nbIter));

		assertTrue("Not SNG with just sb push is invalid", Valid.isValid(
				new NLHEHUPushFoldBuilder<DefaultPlayerNode>(
						new TwoPlusTwoEvaluatorProvider(), 5, 10, 200, 10),
				new DefaultNodesProvider(), nbIter));
	}
}
