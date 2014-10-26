package net.funkyjava.gametheory.gameutil.poker.he.evaluators;

import static org.junit.Assert.assertTrue;
import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.Cards52Strings;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.poker.he.handeval.twoplustwo.TwoPlusTwoEvaluator;
import net.funkyjava.gametheory.gameutil.poker.he.indexing.djhemlig.FlopIndexer;

import org.junit.Test;

/**
 * Test class for {@link EHS2FlopEvaluator}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class EHS2FlopEvaluatorTest {

	/**
	 * Test values based on <a href=
	 * "http://www.poker-ai.org/archive/pokerai.org/pf3/viewtopic57d8.html?f=3&t=2764&view=next"
	 * >Indiana results on poker-ai</a> , with big tolerance (1.5E-3) because
	 * its implementation has a bias (euclidian division for ties/2) <br>
	 * <a href=
	 * "http://www.poker-ai.org/archive/www.pokerai.org/pf3/viewtopic1e13-2.html?f=3&t=444&st=0&sk=t&sd=a&start=20"
	 * >Indiana base implementation</a>
	 */
	@Test
	public void testGetValue() {
		log.info("Testing EHS2 values");
		final FlopIndexer indexer = new FlopIndexer();
		indexer.initialize();
		final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
		Cards52Strings c = new Cards52Strings(specs);
		log.info("Initializing 2+2 evaluator");
		final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
		final EHS2FlopEvaluator ehs = new EHS2FlopEvaluator(specs, eval);
		testValue(0.733, "AsAd", "4cJh9s", c, ehs);
		testValue(0.697, "AhJd", "4cJh9s", c, ehs);
		testValue(0.864, "AhJd", "AcJh9s", c, ehs);
		testValue(0.911, "JdJc", "4cJh9s", c, ehs);
		testValue(0.888, "AhJd", "JcJh9s", c, ehs);
		testValue(0.909, "KhQs", "9sTcJh", c, ehs);
		testValue(0.967, "AhJh", "9hTh4h", c, ehs);
		testValue(0.989, "AhJd", "JcJhAs", c, ehs);
		testValue(1.0, "JhJd", "JcJs4s", c, ehs);
		testValue(1.0, "KsQs", "9sTsJs", c, ehs);
		testValue(0.623, "JsTs", "9s8s3d", c, ehs);
		testValue(0.439, "JsTs", "9c8h3d", c, ehs);
		testValue(0.584, "AsQs", "Js8s3d", c, ehs);
	}

	private void testValue(double value, String handStr, String flopStr,
			Cards52Strings c, EHS2FlopEvaluator ehs) {
		int[][] flop = { c.getCards(handStr), c.getCards(flopStr) };
		double val = ehs.getValue(flop);
		log.info("For hand " + handStr + " flop " + flopStr
				+ " expected EHS2 = " + value + " got " + val);
		assertTrue("For hand " + handStr + " flop " + flopStr
				+ " expected EHS2 = " + value + " but got " + val,
				Math.abs(val - value) < 1.5E-3);
	}

	/**
	 * Monothread performance test
	 * 
	 * @throws Exception
	 */
	@Test
	public void testPerformance() throws Exception {
		log.info("Testing EHS2 evaluator performance");
		final FlopIndexer indexer = new FlopIndexer();
		indexer.initialize();
		final IntCardsSpec specs = DefaultIntCardsSpecs.getDefault();
		Cards52Strings c = new Cards52Strings(specs);
		log.info("Initializing 2+2 evaluator");
		final TwoPlusTwoEvaluator eval = new TwoPlusTwoEvaluator();
		final EHS2FlopEvaluator ehs = new EHS2FlopEvaluator(specs, eval);
		int[][] flop = { c.getCards("AsQs"), c.getCards("Js8s3d") };
		for (int j = 0; j < 10; j++) {
			long start = System.currentTimeMillis();
			final int nb = 100;
			for (int i = 0; i < 100; i++)
				ehs.getValue(flop);
			long time = System.currentTimeMillis() - start;
			log.info("Performed {} EHS2 computations in {} ms, {} iter/s", nb,
					time, nb * 1000 / ((double) time));
		}
	}
}
