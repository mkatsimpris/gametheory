package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.extern.slf4j.Slf4j;
import net.funkyjava.gametheory.gameutil.cards.DefaultIntCardsSpecs;
import net.funkyjava.gametheory.gameutil.cards.IntCardsSpec;
import net.funkyjava.gametheory.gameutil.cards.indexing.CardsGroupsIndexer;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Test class for {@link Double52CardsLUTBuilder}
 * 
 * @author Pierre Mardon
 * 
 */
@Slf4j
public class Double52CardsLUTBuilderTest {

	/**
	 * The temporary folder
	 */
	@ClassRule
	public static TemporaryFolder folder = new TemporaryFolder();

	private static final int lutSize = 1000;

	private final CardsGroupsIndexer indexer = new CardsGroupsIndexer() {

		private int i = 0;

		@Override
		public boolean isCompatible(String gameId) {
			return true;
		}

		@Override
		public int indexOf(int[][] cardsGroups) {
			return i++ % lutSize;
		}

		@Override
		public int getIndexSize() {
			return lutSize;
		}

		@Override
		public IntCardsSpec getCardsSpec() {
			return DefaultIntCardsSpecs.getDefault();
		}

		@Override
		public boolean canHandleGroups(int[] groupsSizes) {
			return true;
		}
	};

	private final CardsGroupsDoubleEvaluatorProvider<?> provider = new CardsGroupsDoubleEvaluatorProvider<CardsGroupsDoubleEvaluator>() {

		@Override
		public CardsGroupsDoubleEvaluator get() {
			return new CardsGroupsDoubleEvaluator() {

				@Override
				public boolean isCompatible(String gameId) {
					return true;
				}

				@Override
				public double getValue(int[][] cardsGroups) {
					return 1;
				}

				@Override
				public IntCardsSpec getCardsSpec() {
					return DefaultIntCardsSpecs.getDefault();
				}

				@Override
				public boolean canHandleGroups(int[] groupsSizes) {
					return true;
				}
			};
		}
	};

	/**
	 * Test building, writing and reading LUT
	 * 
	 * @throws InterruptedException
	 * @throws IOException
	 */
	@Test
	public void testBuildAndWriteLUT() throws InterruptedException, IOException {
		Path path = folder.newFolder("test").toPath().resolve("myLUTTest");
		testBuildAndWriteLUT(path, true, true);
		testBuildAndWriteLUT(path, true, false);
		testBuildAndWriteLUT(path, false, false);
	}

	private void testBuildAndWriteLUT(Path path, boolean countOccurrences,
			boolean meanValues) throws InterruptedException, IOException {
		Double52CardsLUTBuilder.buildAndWriteLUT(indexer, provider, new int[] {
				2, 2 }, 7, meanValues, "", path, countOccurrences);
		DoubleLUT lut;
		double[] table;
		if (countOccurrences) {
			log.info("Reading LUT with occurences");
			lut = DoubleLUT.readFromFile(path, true);
			assertTrue("Occurences and table have not the same length",
					lut.getTable().length == lut.getOccurrences().length);
			assertTrue("Occurences and table have not expected lut size",
					lut.getTable().length == lutSize);
			table = lut.getTable();
			for (int i = 0; i < lutSize; i++)
				assertTrue("Wrong value read from file LUT " + table[i]
						+ " expected 1.0", table[i] == 1.0);
		}
		log.info("Reading LUT without occurences");
		lut = DoubleLUT.readFromFile(path, false);
		assertTrue("Occurences should be null", lut.getOccurrences() == null);
		assertTrue("Table has not expected lut size",
				lut.getTable().length == lutSize);
		table = lut.getTable();
		for (int i = 0; i < lutSize; i++)
			assertTrue("Wrong value read from file LUT " + table[i]
					+ " expected 1.0", table[i] == 1.0);
		Files.delete(path);
	}
}
