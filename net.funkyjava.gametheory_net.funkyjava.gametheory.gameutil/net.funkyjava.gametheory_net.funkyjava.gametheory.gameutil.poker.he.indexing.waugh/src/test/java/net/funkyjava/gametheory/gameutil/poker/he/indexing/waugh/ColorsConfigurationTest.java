package net.funkyjava.gametheory.gameutil.poker.he.indexing.waugh;

import lombok.extern.slf4j.Slf4j;

import org.junit.Assert;
import org.junit.Test;

@Slf4j
public class ColorsConfigurationTest extends ColorsConfiguration {

	public ColorsConfigurationTest() {
		super(
				new int[][] { { 1, 2, 0 }, { 1, 0, 0 }, { 0, 0, 1 },
						{ 0, 0, 1 } });
		// TODO Auto-generated constructor stub
	}

	@Test
	public void test() {
		final int size = this.getSize();
		final int[] colorsIdxs = new int[4];
		for (int i = 0; i < size; i++) {
			this.unindexIdxsForConf(i, colorsIdxs);
			Assert.assertTrue(i == this.indexIdxsForConf(colorsIdxs));
		}
		System.out.println(size);
	}
}
