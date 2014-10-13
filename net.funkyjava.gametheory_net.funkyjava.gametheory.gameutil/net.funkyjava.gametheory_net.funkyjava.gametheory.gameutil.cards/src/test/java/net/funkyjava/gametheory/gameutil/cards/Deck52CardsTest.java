package net.funkyjava.gametheory.gameutil.cards;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import lombok.extern.slf4j.Slf4j;

import org.junit.Test;

/**
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class Deck52CardsTest {

	/**
	 * Test one shot draws
	 * 
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testOneShotDeckDraw() throws Exception {
		Deck52Cards d = new Deck52Cards(1);
		int[] fullDeck = new int[52];
		d.oneShotDeckDraw(fullDeck);
		log.info("Full deck drawing : {}", fullDeck);
		mainloop: for (int i = 1; i < 53; i++) {
			for (int j = 0; j < 52; j++)
				if (fullDeck[j] == i)
					continue mainloop;
			fail("The card " + i + " wasn't found");
		}
		assertTrue("The size of the deck should be 52", d.getSize() == 52);
		int[] heHUCards = new int[9];
		long start = System.currentTimeMillis();
		int nbDrawing = 1000000;
		for (int i = 0; i < nbDrawing; i++)
			d.oneShotDeckDraw(heHUCards);
		double val;
		log.info(
				"{} Full Hold'em Heads Up drawings in {}ms, {} drawings per second",
				nbDrawing, val = (System.currentTimeMillis() - start),
				nbDrawing * 1000 / val);
	}

	/**
	 * @throws Exception
	 *             unexpected exception
	 */
	@Test
	public void testDraw() throws Exception {
		Deck52Cards d = new Deck52Cards(1);
		int[] fullDeck = new int[52];
		d.draw(fullDeck);
		log.info("Full deck drawing : {}", fullDeck);
		mainloop: for (int i = 1; i < 53; i++) {
			for (int j = 0; j < 52; j++)
				if (fullDeck[j] == i)
					continue mainloop;
			fail("The card " + i + " wasn't found");
		}
		assertTrue("The deck should be empty", d.getSize() == 0);
		int[] heHUCards = new int[9];
		int nbDrawing = 1000000;
		long start = System.currentTimeMillis();
		for (int i = 0; i < nbDrawing; i++) {
			d.reset();
			d.draw(heHUCards);
		}
		double val;
		log.info(
				"{} Full Hold'em Heads Up drawings in {}ms, {} drawings per second",
				nbDrawing, val = (System.currentTimeMillis() - start),
				nbDrawing * 1000 / val);
	}
}
