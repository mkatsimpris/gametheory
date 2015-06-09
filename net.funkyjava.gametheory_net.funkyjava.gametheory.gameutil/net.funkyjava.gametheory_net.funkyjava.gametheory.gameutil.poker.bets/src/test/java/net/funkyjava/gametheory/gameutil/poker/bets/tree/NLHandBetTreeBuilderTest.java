package net.funkyjava.gametheory.gameutil.poker.bets.tree;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.poker.bets.BlindsAnteSpec;
import net.funkyjava.gametheory.gameutil.poker.bets.BlindsAnteSpec.BlindsAnteSpecBuilder;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.NLHandRounds;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.BlindsAnteParameters;
import net.funkyjava.gametheory.gameutil.poker.bets.rounds.data.NoBetPlayersData;

import org.junit.Test;

/**
 * Test class for {@link NLHandBetTreeBuilder} TODO complete test set
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class NLHandBetTreeBuilderTest {

	/**
	 * Simply test the bet tree generation for an arbitrary situation
	 */
	@Test
	public void test() {
		NoBetPlayersData playersData = new NoBetPlayersData(new int[] { 1000,
				1000 }, new boolean[] { true, true });
		BlindsAnteSpecBuilder builder = BlindsAnteSpec.builder();
		builder.sbValue(10);
		builder.bbValue(20);
		builder.sbIndex(0);
		builder.bbIndex(1);
		builder.anteValue(0);
		builder.enableAnte(false);
		builder.enableBlinds(true);
		builder.isCash(false);
		builder.firstPlayerAfterBlinds(0);
		builder.shouldPostEnteringBb(new boolean[] { false, true });
		NLHandRounds hand = new NLHandRounds(new BlindsAnteParameters(
				playersData, builder.build()), 4, 1, false);
		NLPushFoldBetRangeSlicer slicer = new NLPushFoldBetRangeSlicer();
		NLIndexedBetTree bTree = NLHandBetTreeBuilder.getTree(hand, slicer);
		log.info("Finished building Push/Fold bet tree");
		log.info("Nb of bet nodes {}", bTree.getNbOfBetNodes());
		NLBetTreeNode.walkTree(bTree.getRootNode(),
				new SimpleNLBetTreePrinter());
	}
}
