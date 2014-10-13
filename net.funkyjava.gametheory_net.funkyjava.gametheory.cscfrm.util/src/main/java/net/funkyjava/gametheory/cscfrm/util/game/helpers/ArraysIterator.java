/**
 * 
 */
package net.funkyjava.gametheory.cscfrm.util.game.helpers;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Iterators;

/**
 * 
 * Static helper to easily build iterators over arrays of up to four dimensions.
 * Meant to be used to implement player nodes iterators for games.
 * 
 * @author Pierre Mardon
 * 
 */
public class ArraysIterator {

	/**
	 * Private constructor, it's a static helper
	 */
	private ArraysIterator() {
	}

	/**
	 * Create an iterator over an array of elements
	 * 
	 * @param <T>
	 *            the elements type
	 * @param elems
	 *            the elements
	 * @return the iterator
	 */
	@SafeVarargs
	public static <T> Iterator<T> get(T... elems) {
		checkNotNull(elems, "Cannot create iterator over null elements");
		return Arrays.asList(elems).iterator();
	}

	/**
	 * Create an iterator over multiple arrays of elements
	 * 
	 * @param <T>
	 *            the elements type
	 * @param arrays
	 *            one dimension arrays
	 * @return the iterator
	 */
	@SafeVarargs
	public static <T> Iterator<T> get(T[]... arrays) {
		checkNotNull(arrays, "Cannot create iterator over null arrays");
		List<Iterator<T>> list = new LinkedList<>();
		for (T[] arr : arrays)
			list.add(get(arr));
		return Iterators.concat(list.iterator());
	}

	/**
	 * Create an iterator over multiple two dimensions arrays of elements
	 * 
	 * @param <T>
	 *            the elements type
	 * @param arrays
	 *            two dimensions arrays
	 * @return the iterator
	 */
	@SafeVarargs
	public static <T> Iterator<T> get(T[][]... arrays) {
		checkNotNull(arrays, "Cannot create iterator over null arrays");
		List<Iterator<T>> list = new LinkedList<>();
		for (T[][] arr : arrays)
			list.add(get(checkNotNull(arr)));
		return Iterators.concat(list.iterator());
	}

	/**
	 * Create an iterator over multiple three dimensions arrays of elements
	 * 
	 * @param <T>
	 *            the elements type
	 * @param arrays
	 *            three dimensions arrays
	 * @return the iterator
	 */
	@SafeVarargs
	public static <T> Iterator<T> get(T[][][]... arrays) {
		checkNotNull(arrays, "Cannot create iterator over null arrays");
		List<Iterator<T>> list = new LinkedList<>();
		for (T[][][] arr : arrays)
			list.add(get(checkNotNull(arr)));
		return Iterators.concat(list.iterator());
	}
}
