package net.funkyjava.gametheory.cscfrm.model.game;

import net.funkyjava.gametheory.commonmodel.game.GameChancePicker;
import net.funkyjava.gametheory.commonmodel.game.GamePlayerDecider;
import net.funkyjava.gametheory.commonmodel.game.GameTreeWalker;
import net.funkyjava.gametheory.cscfrm.model.game.nodes.PlayerNode;

/**
 * A {@link CSCFRMFullGame} is a game that has all good properties to make it
 * available to :
 * <ul>
 * <li>Run CSCFRM algorithm via its {@link CSCFRMBaseGame} interface</li>
 * <li>Walk the game tree via its {@link GameTreeWalker} interface</li>
 * <li>Make player(s) decisions during tree walk via its
 * {@link GamePlayerDecider} interface</li>
 * <li>Pick chances during tree walk via its {@link GameChancePicker} interface</li>
 * <li>Save, load and share players nodes via its {@link CSCFRMGame} interface</li>
 * </ul>
 * .
 * 
 * @param <PNode>
 *            the player nodes type
 */
public interface CSCFRMFullGame<PNode extends PlayerNode> extends
		CSCFRMGame<PNode>, GameTreeWalker, GamePlayerDecider {

}
