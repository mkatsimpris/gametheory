package net.funkyjava.gametheory.gameutil.cards.indexing.bucketing;

import static com.google.common.base.Preconditions.checkArgument;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Set;

import lombok.NonNull;

/**
 * A look-up table containing multiple double values for each index and
 * optionally an occurrences count for each index
 * 
 * @author Pierre Mardon
 * 
 */
public class MultiDoubleLUT {
	@NonNull
	private final double[] table;
	private final int[] occurrences;
	private final int nbIndexes, nbValuesPerIndex;

	/**
	 * Constructor for an empty table
	 * 
	 * @param nbIndexes
	 *            number of indexes in the LUT
	 * @param nbValuesPerIndex
	 *            number double of values per index
	 * @param countOccurrences
	 *            indicates whether an occurrences count table must be
	 *            instantiated or not
	 */
	public MultiDoubleLUT(int nbIndexes, int nbValuesPerIndex,
			boolean countOccurrences) {
		checkArgument(nbIndexes > 0, "The number of indexes is <= 0");
		checkArgument(nbValuesPerIndex > 0,
				"The number of values per index is <= 0");
		this.nbIndexes = nbIndexes;
		this.nbValuesPerIndex = nbValuesPerIndex;
		table = new double[nbIndexes * nbValuesPerIndex];
		occurrences = countOccurrences ? new int[nbIndexes] : null;
	}

	/**
	 * Constructor for an existing table
	 * 
	 * @param table
	 *            the LUT values
	 * @param occurrences
	 *            indexes occurrences count table
	 * @param nbValuesPerIndex
	 *            number of double values per index
	 */
	public MultiDoubleLUT(@NonNull double[] table, int[] occurrences,
			int nbValuesPerIndex) {
		this.table = table;
		this.occurrences = occurrences;
		this.nbValuesPerIndex = nbValuesPerIndex;
		this.nbIndexes = table.length / nbValuesPerIndex;
		checkArgument(table.length % nbValuesPerIndex == 0,
				"Table size is not a multiple of nbValuesPerIndex");
		if (occurrences != null)
			checkArgument(nbIndexes == occurrences.length,
					"Occurrences table has not the expected size");
	}

	/**
	 * Gets the values table
	 * 
	 * @return the values table
	 */
	public double[] getTable() {
		return table;
	}

	/**
	 * Gets the occurrences counts table
	 * 
	 * @return the occurrences counts table
	 */
	public int[] getOccurrences() {
		return occurrences;
	}

	/**
	 * Gets the index size
	 * 
	 * @return the index size
	 */
	public int getIndexSize() {
		return nbIndexes;
	}

	/**
	 * Gets the number of values per index
	 * 
	 * @return the number of values per index
	 */
	public int getNbValuesPerIndex() {
		return nbValuesPerIndex;
	}

	/**
	 * Gets one of the values for a given cards groups index
	 * 
	 * @param index
	 *            the index
	 * @param valueIndex
	 *            the value index
	 * @return the value
	 */
	public double getValue(int index, int valueIndex) {
		return table[index * nbValuesPerIndex + valueIndex];
	}

	/**
	 * Read the values for a given index. The number of read values will be the
	 * destination array length, starting with the first value.
	 * 
	 * @param index
	 *            the index
	 * @param dest
	 *            destination array
	 */
	public void readValues(int index, double[] dest) {
		System.arraycopy(table, index * nbValuesPerIndex, dest, 0, dest.length);
	}

	/**
	 * Read values for a given index. The number of read values will be the
	 * destination array length, starting with the value of index
	 * firstValueIndex.
	 * 
	 * @param index
	 *            the cards groups index
	 * @param firstValueIndex
	 *            the first value to read index
	 * @param dest
	 *            the destination array
	 */
	public void readValues(int index, int firstValueIndex, double[] dest) {
		System.arraycopy(table, index * nbValuesPerIndex + firstValueIndex,
				dest, 0, dest.length);
	}

	/**
	 * Set a value for an index
	 * 
	 * @param index
	 *            the cards groups index
	 * @param valueIndex
	 *            the value index
	 * @param value
	 *            the value
	 */
	public void setValue(int index, int valueIndex, double value) {
		table[nbValuesPerIndex * index + valueIndex] = value;
	}

	/**
	 * Adds a value for an index
	 * 
	 * @param index
	 *            the cards groups index
	 * @param valueIndex
	 *            the value index
	 * @param value
	 *            the value
	 */
	public void addValue(int index, int valueIndex, double value) {
		table[nbValuesPerIndex * index + valueIndex] += value;
	}

	/**
	 * Set values for an index. The number of set values will be the values
	 * array length, starting with the value of index firstValueIndex.
	 * 
	 * @param index
	 *            the cards groups index
	 * @param firstValueIndex
	 *            the first value to set index
	 * @param values
	 *            the values array
	 */
	public void setValues(int index, int firstValueIndex, double[] values) {
		System.arraycopy(values, 0, table, index * nbValuesPerIndex
				+ firstValueIndex, values.length);
	}

	/**
	 * Set values for an index. The number of set values will be the values
	 * array length, starting with the first value.
	 * 
	 * @param index
	 *            the cards groups index
	 * @param values
	 *            the values array
	 */
	public void setValues(int index, double[] values) {
		System.arraycopy(values, 0, table, index * nbValuesPerIndex,
				values.length);
	}

	/**
	 * Add values for an index. The number of added values will be the values
	 * array length, starting with the value of index firstValueIndex.
	 * 
	 * @param index
	 *            the cards groups index
	 * @param firstValueIndex
	 *            the first value to add index
	 * @param values
	 *            the values array
	 */
	public void addValues(int index, int firstValueIndex, double[] values) {
		for (int i = 0; i < values.length; i++)
			table[index * nbValuesPerIndex + firstValueIndex + i] += values[i];
	}

	/**
	 * Add values for an index. The number of added values will be the values
	 * array length, starting with the first value.
	 * 
	 * @param index
	 *            the cards groups index
	 * @param values
	 *            the values array
	 */
	public void addValues(int index, double[] values) {
		for (int i = 0; i < values.length; i++)
			table[index * nbValuesPerIndex + i] += values[i];
	}

	/**
	 * Gets the number of occurrences for a given index.
	 * 
	 * @param index
	 *            the index
	 * @return the number of occurrences
	 */
	public int getOccurrences(int index) {
		return occurrences[index];
	}

	/**
	 * Sets the number of occurrences for a given index.
	 * 
	 * @param index
	 *            the index
	 * @param nbOccurences
	 *            the number of occurrences
	 */
	public void setOccurences(int index, int nbOccurences) {
		occurrences[index] = nbOccurences;
	}

	/**
	 * Increments the number of occurrences for a given index.
	 * 
	 * @param index
	 *            the index
	 */
	public void incrOccurences(int index) {
		occurrences[index]++;
	}

	/**
	 * Mean all indexes values. Expects that for each index, for each
	 * occurrence, all values has been added and the occurrences count has been
	 * incremented properly.
	 */
	public void meanValues() {
		for (int i = 0; i < nbIndexes; i++)
			for (int j = 0; j < nbValuesPerIndex; j++)
				table[i * nbValuesPerIndex + j] /= occurrences[i];
	}

	private final static Set<OpenOption> fileOptions = new HashSet<OpenOption>();
	static {
		fileOptions.add(StandardOpenOption.WRITE);
		fileOptions.add(StandardOpenOption.READ);
		fileOptions.add(StandardOpenOption.CREATE);
	}

	/**
	 * Write a look-up table to a non-existing file
	 * 
	 * @param path
	 *            the path of the file to write to
	 * @param writeOccurences
	 *            specifies if occurences counts must be written
	 * @throws IOException
	 *             can be thrown if file exist, parent path doesn't exist or
	 *             rights are missing
	 */
	public void writeToFile(Path path, boolean writeOccurences)
			throws IOException {
		if (Files.exists(path))
			throw new IOException("File at path " + path + " already exists");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, fileOptions);
		buffer.putInt(nbIndexes);
		buffer.putInt(nbValuesPerIndex);
		writeBuffer(chan, buffer, 8);
		buffer.putInt(writeOccurences ? 1 : 0);
		writeBuffer(chan, buffer, 4);
		buffer.rewind().limit(8);
		for (double val : table) {
			buffer.putDouble(val);
			writeBuffer(chan, buffer, 8);
		}
		if (writeOccurences)
			for (int occ : occurrences) {
				buffer.putInt(occ);
				writeBuffer(chan, buffer, 4);
			}
		chan.close();
	}

	/**
	 * Read a {@link MultiDoubleLUT} from a file
	 * 
	 * @param path
	 *            the target file
	 * @param getOccurences
	 *            specifies if occurences must be read
	 * @return the double LUT
	 * @throws IOException
	 *             whenever there is a problem
	 */
	public static MultiDoubleLUT readFromFile(Path path, boolean getOccurences)
			throws IOException {
		if (!Files.exists(path))
			throw new IOException("File at path " + path.toString()
					+ " doesn't exist");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, fileOptions);
		read(chan, buffer, 8);
		final int indexSize = buffer.getInt();
		final int nbValuesPerIndex = buffer.getInt();
		read(chan, buffer, 4);
		final boolean writeOccurences = buffer.getInt() > 0;
		checkArgument(!getOccurences || writeOccurences,
				"Trying to get LUT occurences count but they were not written");
		final double[] table = new double[indexSize * nbValuesPerIndex];
		final int[] occurences = getOccurences ? new int[indexSize] : null;
		for (int i = 0; i < indexSize * nbValuesPerIndex; i++) {
			read(chan, buffer, 8);
			table[i] = buffer.getDouble();
		}
		if (getOccurences)
			for (int i = 0; i < indexSize; i++) {
				read(chan, buffer, 4);
				occurences[i] = buffer.getInt();
			}
		chan.close();
		return new MultiDoubleLUT(table, occurences, nbValuesPerIndex);
	}

	private static void writeBuffer(FileChannel chan, ByteBuffer buffer,
			int nbBytes) throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.write(buffer) != nbBytes)
			throw new IOException("Couldn't write all " + nbBytes + " bytes");
		buffer.rewind();
	}

	private static void read(FileChannel chan, ByteBuffer buffer, int nbBytes)
			throws IOException {
		buffer.rewind().limit(nbBytes);
		if (chan.read(buffer) != nbBytes)
			throw new IOException("Couldn't read all " + nbBytes + " bytes");
		buffer.rewind();
	}

}