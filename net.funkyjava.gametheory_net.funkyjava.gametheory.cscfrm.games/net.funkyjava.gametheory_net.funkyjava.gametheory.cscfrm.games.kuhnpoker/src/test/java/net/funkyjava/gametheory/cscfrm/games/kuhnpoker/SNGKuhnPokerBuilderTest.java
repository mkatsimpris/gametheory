package net.funkyjava.gametheory.cscfrm.games.kuhnpoker;


import net.funkyjava.gametheory.cscfrm.impl.game.nodes.DefaultPlayerNode;
import net.funkyjava.gametheory.cscfrm.impl.game.nodes.provider.DefaultNodesProvider;
import net.funkyjava.gametheory.cscfrm.util.game.validation.Valid;

import org.junit.Test;

/**
 * @author Pierre Mardon
 * 
 *         TODO validate error cases
 */
public class SNGKuhnPokerBuilderTest {

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testSNGKuhnPokerBuilder() throws Exception {
		Valid.isValid(new SNGKuhnPokerBuilder<DefaultPlayerNode>(10),
				new DefaultNodesProvider(), 100000);
	}
}
