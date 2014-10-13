package net.funkyjava.gametheory.cscfrm.model.game.cyclic;

import java.util.List;

import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGame;
import net.funkyjava.gametheory.cscfrm.model.game.CSCFRMGameBuilder;
import net.funkyjava.gametheory.commonmodel.game.CyclicStepsGamePositionMapper;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.Node;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;
import net.funkyjava.gametheory.commonmodel.game.nodes.MinPublicNode;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.provider.NodesProvider;

/**
 * <p>
 * The interface CSCFRMCyclicStepsGameBuilder describes a global game composed
 * of a determined number of steps which are separately eligible to CSCFRM. In
 * each step, every terminal node can indicate that its payoffs should be
 * obtained from another (or the same) step's utility.
 * </p>
 * 
 * <p>
 * It will be eligible to a global multi-CFRM algorithm with sliding utilities.
 * </p>
 * 
 * <p>
 * In order to perform the reference to a step's utility, a terminal node has to
 * provide an id > 0 that matches the target next step's index in the builder's
 * list.
 * </p>
 * 
 * @author Pierre Mardon
 * 
 * @param <PNode>
 *            the player nodes type
 * @param <StepGameClass>
 *            the step game type
 * @see Node#id
 * @see MinPublicNode#getId()
 * @see NodesProvider#getTerminalNode(int)
 * @see NodesProvider#getTerminalNode(int)
 */
public interface CSCFRMCyclicStepsGameBuilder<PNode extends PlayerNode, StepGameClass extends CSCFRMGame<PNode>>
		extends CyclicStepsGamePositionMapper {

	/**
	 * Gets the number players. It must be the same for every step of the global
	 * game.
	 * 
	 * @return the number players
	 */
	int getNbPlayers();

	/**
	 * Gets the steps builders. The steps indexes in the list must match the
	 * terminal nodes ids.
	 * 
	 * @return the builders
	 */
	List<CSCFRMGameBuilder<PNode, StepGameClass>> getBuilders();

	/**
	 * <p>
	 * Gets the global game unique id. If it is parameterized, this id should
	 * take it into account so that there can be no confusion. To keep
	 * coherence, it should also include a version number. This id is typically
	 * used to write persistent iteration data into files.
	 * </p>
	 * <p>
	 * Caution : this id identifies game implementation but should not be
	 * instance-dependent.
	 * </p>
	 * 
	 * @return the global game unique id
	 */
	String getUId();
}
