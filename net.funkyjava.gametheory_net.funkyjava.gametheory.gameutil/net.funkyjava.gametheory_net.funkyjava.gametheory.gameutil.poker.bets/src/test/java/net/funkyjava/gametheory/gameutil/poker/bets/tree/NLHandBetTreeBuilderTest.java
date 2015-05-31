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
		BettingTree bTree = NLHandBetTreeBuilder.getBetTree(hand, slicer);
		RoundBetTree[] trees = bTree.getRoundBetTrees();
		int nbNodes = 0;
		int nbEdges = 0;
		for (RoundBetTree tree : trees) {
			nbNodes += tree.getNbNodes();
			nbEdges += tree.getNbEdges();
			log.info("Bet nodes {} edges {}", tree.getNbNodes(),
					tree.getNbEdges());
		}
		log.info("Total bet nodes {} edges {}", nbNodes, nbEdges);
		log.info("Term nodes {} showdown {} noShowdown {}", bTree
				.getTermNodes().getNbNodes(),

		bTree.getTermNodes().getNbShowdownNodes(),

		bTree.getTermNodes().getNbNoShowdownNodes());
		nbNodes += bTree.getTermNodes().getNbNodes();
		log.info("Total nodes {} edges {}", nbNodes, nbEdges);
		// HandBetTreeBuilder.doAnteAndBlinds(hand);
		// hand.doMove(Move.getRaise(0, 1000, 10));
		// log.info("{}", hand.getRoundState());
	}
}
