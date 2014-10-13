/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.validation;

import java.util.Iterator;

import lombok.extern.slf4j.Slf4j;

/**
 * Provides a static method to ensure that two iterators iterate over the same
 * set and in the same order.
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class IteratorsIdentityValidator {

	/**
	 * Private constructor
	 */
	private IteratorsIdentityValidator() {
	}

	/**
	 * Ensures that two iterators iterate over the same set and in the same
	 * order.
	 * 
	 * @param <P>
	 *            the class of the objects to iterate on
	 * @param it
	 *            the first iterator
	 * @param it2
	 *            the second iterator
	 * @return true when all objects are the same and in the same order
	 */
	public static <P> boolean areTheSame(Iterator<P> it, Iterator<P> it2) {
		if (it == null || it2 == null) {
			log.error("Validation failed, provided iterator is null");
			return false;
		}
		if (it == it2) {
			log.error("Cannot compare an Iterator to itself.");
			return false;
		}
		P p, p2;
		while (it.hasNext() && it2.hasNext()) {
			p = it.next();
			if (p == null) {
				log.error("Iterator validation failed : provided object is null.");
				return false;
			}
			p2 = it2.next();
			if (p2 == null) {
				log.error("Iterator validation failed : provided object is null.");
				return false;
			}
			if (p != p2) {
				log.error("Iterators don't seem to provide the objects in the same order");
				return false;
			}
		}
		if (it.hasNext() || it2.hasNext()) {
			log.error("The iterators don't have the same elements count.");
			return false;
		}
		return true;
	}
}
