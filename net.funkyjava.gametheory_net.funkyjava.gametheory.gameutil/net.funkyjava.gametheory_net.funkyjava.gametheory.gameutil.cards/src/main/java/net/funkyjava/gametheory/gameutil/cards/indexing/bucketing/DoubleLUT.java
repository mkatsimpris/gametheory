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
 * A look-up table containing double values and optionally an occurrences count
 * for each index
 * 
 * @author Pierre Mardon
 * 
 */
public class DoubleLUT {

	@NonNull
	private final double[] table;
	private final int[] occurrences;

	private final static Set<OpenOption> fileOptions = new HashSet<OpenOption>();
	static {
		fileOptions.add(StandardOpenOption.WRITE);
		fileOptions.add(StandardOpenOption.READ);
		fileOptions.add(StandardOpenOption.CREATE);
	}

	/**
	 * Constructor
	 * 
	 * @param indexSize
	 *            size of the index
	 * @param countOccurrences
	 *            when true, the occurrences table will be instantiated
	 */
	public DoubleLUT(int indexSize, boolean countOccurrences) {
		this(new double[indexSize], countOccurrences ? new int[indexSize]
				: null);
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            the table containing values
	 * @param occurrences
	 *            the table counting occurrences for each index
	 */
	public DoubleLUT(@NonNull double[] table, int[] occurrences) {
		this.table = table;
		this.occurrences = occurrences;
		if (occurrences != null)
			checkArgument(occurrences.length == table.length,
					"Occurrences table has not the same length as the values table");
	}

	/**
	 * Constructor
	 * 
	 * @param table
	 *            the table containing values
	 */
	public DoubleLUT(@NonNull double[] table) {
		this(table, null);
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
	 * Gets the value of the LUT for a specific index
	 * 
	 * @param index
	 *            the index to look for
	 * @return the value
	 */
	public double getValueFor(int index) {
		return table[index];
	}

	/**
	 * Gets the occurrences count for a given index
	 * 
	 * @param index
	 *            the index
	 * @return the occurrences count for this index
	 */
	public int getOccurencesCountFor(int index) {
		return occurrences[index];
	}

	/**
	 * Sets the value of the LUT for a specific index
	 * 
	 * @param index
	 *            the index to set the value
	 * @param value
	 *            the value to set
	 */
	public void setValueFor(int index, double value) {
		table[index] = value;
	}

	/**
	 * Sets the occurrences count for a given index
	 * 
	 * @param index
	 *            the index
	 * @param count
	 *            the count to set
	 */
	public void getOccurrencesCountFor(int index, int count) {
		occurrences[index] = count;
	}

	/**
	 * Add value to the LUT for a specific index
	 * 
	 * @param index
	 *            the index to add the value
	 * @param value
	 *            the value to add
	 */
	public void addValueFor(int index, double value) {
		table[index] += value;
	}

	/**
	 * Increments the occurrences count for a given index
	 * 
	 * @param index
	 *            the index
	 */
	public void incrOccurrencesCountFor(int index) {
		occurrences[index]++;
	}

	/**
	 * Mean each index value. Expects that for each index, for each occurrence,
	 * a value has been added and the occurrences count has been incremented
	 */
	public void meanValues() {
		for (int i = 0; i < table.length; i++)
			table[i] /= occurrences[i];
	}

	/**
	 * Write a look-up table to a non-existing file
	 * 
	 * @param path
	 *            the path of the file to write to
	 * @param writeOccurrences
	 *            specifies if occurrences counts must be written
	 * @throws IOException
	 *             can be thrown if file exist, parent path doesn't exist or
	 *             rights are missing
	 */
	public void writeToFile(Path path, boolean writeOccurrences)
			throws IOException {
		if (Files.exists(path))
			throw new IOException("File at path " + path + " already exists");
		checkArgument(!writeOccurrences || occurrences != null,
				"Cant write occurrences, they were not generated.");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, fileOptions);
		final int indexSize = table.length;
		buffer.putInt(indexSize);
		buffer.putInt(writeOccurrences ? 1 : 0);
		writeBuffer(chan, buffer, 8);
		for (double val : table) {
			buffer.putDouble(val);
			writeBuffer(chan, buffer, 8);
		}
		if (writeOccurrences)
			for (int occ : occurrences) {
				buffer.putInt(occ);
				writeBuffer(chan, buffer, 4);
			}
		chan.close();
	}

	/**
	 * Read a {@link DoubleLUT} from a file
	 * 
	 * @param path
	 *            the target file
	 * @param getOccurrences
	 *            specifies if occurrences must be read
	 * @return the double LUT
	 * @throws IOException
	 *             whenever there is a problem
	 */
	public static DoubleLUT readFromFile(Path path, boolean getOccurrences)
			throws IOException {
		if (!Files.exists(path))
			throw new IOException("File at path " + path.toString()
					+ " doesn't exist");
		final ByteBuffer buffer = ByteBuffer.allocate(8);
		final FileChannel chan = FileChannel.open(path, fileOptions);
		read(chan, buffer, 8);
		final int indexSize = buffer.getInt();
		final boolean writeOccurences = buffer.getInt() > 0;
		checkArgument(!getOccurrences || writeOccurences,
				"Trying to get LUT occurrences count but they were not written");
		final double[] table = new double[indexSize];
		final int[] occurences = getOccurrences ? new int[indexSize] : null;
		for (int i = 0; i < indexSize; i++) {
			read(chan, buffer, 8);
			table[i] = buffer.getDouble();
		}
		if (getOccurrences)
			for (int i = 0; i < indexSize; i++) {
				read(chan, buffer, 4);
				occurences[i] = buffer.getInt();
			}
		chan.close();
		return new DoubleLUT(table, occurences);
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
