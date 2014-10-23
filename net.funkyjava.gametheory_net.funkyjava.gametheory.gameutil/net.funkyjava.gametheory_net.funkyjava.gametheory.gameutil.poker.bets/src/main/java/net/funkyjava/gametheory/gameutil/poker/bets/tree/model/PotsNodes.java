package net.funkyjava.gametheory.gameutil.poker.bets.tree.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.funkyjava.gametheory.gameutil.poker.bets.pots.Pot;

/**
 * A terminal node containing pots. Can designate a hand end with or without
 * showdown
 * 
 * @author Pierre Mardon
 * 
 */
@AllArgsConstructor
public class PotsNodes {

	/**
	 * The pots list for this node
	 */
	@Getter
	public final List<Pot<Integer>> pots;

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof PotsNodes))
			return false;
		PotsNodes pn = (PotsNodes) o;
		if (pn.pots.size() != pots.size())
			return false;
		for (Pot<Integer> p : pots)
			if (!pn.pots.contains(p))
				return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Pot<Integer> p : pots)
			sb.append(p + "\n");
		return sb.toString();
	}
}
