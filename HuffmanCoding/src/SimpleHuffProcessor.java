/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, Jacob Hungerford and Miles Chandler, this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used: 1
 *
 *  Student 1 Jacob Hungerford
 *  UTEID: jdh5468
 *  email address: JHungerford1516@utexas.edu
 *  Grader name: Anthony
 *
 *  Student 2
 *  UTEID: mac9325 Miles Chandler
 *  email address: miles.chandler@ichandler.net
 *
 */

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

// A class used to perform Huffman compression and decompress files compressed using the Huffman compression algorithm.
public class SimpleHuffProcessor implements IHuffProcessor {

	final static int EMPTY_NODE = -1;

	private IHuffViewer myViewer;
	private HuffmanTree huffmanTree;
	private HashMap<Integer, String> codeMap;
	private int bitsSaved;
	private int headerFormat;
	private int[] frequencies;

	/**
	 * Compresses input to output, where the same InputStream has previously
	 * been pre-processed via <code>preprocessCompress</code> storing state used
	 * by this call. <br>
	 * pre: <code>preprocessCompress</code> must be called before this method
	 * 
	 * @param in
	 *            is the stream being compressed (NOT a BitInputStream)
	 * @param out
	 *            is bound to a file/stream to which bits are written for the
	 *            compressed file (not a BitOutputStream)
	 * @param force
	 *            if this is true create the output file even if it is larger
	 *            than the input file. If this is false do not create the output
	 *            file if it is larger than the input file.
	 * @return the number of bits written.
	 * @throws IOException
	 *             if an error occurs while reading from the input file or
	 *             writing to the output file.
	 */
	public int compress(InputStream in, OutputStream out, boolean force)
			throws IOException {
		int compressedBits = 0;
		// Only writes if space is saved or compression is forced.
		if (bitsSaved > 0 || force) {
			BitInputStream bitIn = new BitInputStream(
					new BufferedInputStream(in));
			BitOutputStream bitOut = new BitOutputStream(
					new BufferedOutputStream(out));
			String eof = codeMap.get(PSEUDO_EOF);
			// Write the magic number and the header format to the file.
			compressedBits += BITS_PER_INT * 2 + eof.length();
			bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
			bitOut.writeBits(BITS_PER_INT, headerFormat);
			if (headerFormat == STORE_COUNTS) {
				compressedBits += countHeader(bitOut);
			} else if (headerFormat == STORE_TREE) {
				compressedBits += treeHeader(bitOut);
			} else {
				// Unknown header format.
				bitIn.close();
				bitOut.close();
				return -1;
			}
			// Writes the compressed data and the eof value.
			compressedBits += compressedData(bitOut, bitIn);
			bitOut.writeBits(eof.length(), Integer.valueOf(eof, 2));
			bitOut.close();
		}
		return compressedBits;
	}

	// Write the standard count header and counts the number of bits written.
	private int countHeader(BitOutputStream bitOut) {
		for (int i = 0; i < ALPH_SIZE; i++) {
			bitOut.writeBits(BITS_PER_INT, frequencies[i]);
		}
		return ALPH_SIZE * BITS_PER_INT;
	}

	// Write the standard tree header and counts the number of bits written.
	private int treeHeader(BitOutputStream bitOut) {
		// Writes out the size of the tree in integer form.
		bitOut.writeBits(BITS_PER_INT, huffmanTree.getEncodeSize());
		int bits = BITS_PER_INT;
		for (TreeNode node : huffmanTree.getNodeList()) {
			int value = node.getValue();
			bits++;
			// If the node has no character value write a 0.
			if (value == EMPTY_NODE) {
				bitOut.writeBits(1, 0);
				// Else write a 1 and the value of the character in binary.
			} else {
				bitOut.writeBits(1, 1);
				// then write the value of the node
				bitOut.writeBits(BITS_PER_WORD + 1, value);
				bits += BITS_PER_WORD + 1;
			}
		}
		return bits;
	}

	// Writes out the compressed form of the original data and counts the bits
	// written.
	private int compressedData(BitOutputStream bitOut, BitInputStream bitIn)
			throws IOException {
		int inBits;
		int bits = 0;
		// While there is still data to read get the code for the "word" and
		// write it to the compressed file.
		while ((inBits = bitIn.readBits(BITS_PER_WORD)) != -1) {
			String code = codeMap.get(inBits);
			int length = code.length();
			bitOut.writeBits(length, Integer.valueOf(code, 2));
			bits += length;
		}
		bitIn.close();
		return bits;
	}

	/**
	 * Preprocess data so that compression is possible --- count
	 * characters/create tree/store state so that a subsequent call to compress
	 * will work. The InputStream is <em>not</em> a BitInputStream, so wrap it
	 * int one as needed.
	 * 
	 * @param in
	 *            is the stream which could be subsequently compressed
	 * @param headerFormat
	 *            a constant from IHuffProcessor that determines what kind of
	 *            header to use, standard count format, standard tree format, or
	 *            possibly some format added in the future.
	 * @return number of bits saved by compression or some other measure Note,
	 *         to determine the number of bits saved, the number of bits written
	 *         includes ALL bits that will be written including the magic
	 *         number, the header format number, the header to reproduce the
	 *         tree, AND the actual data.
	 * @throws IOException
	 *             if an error occurs while reading from the input file.
	 */
	public int preprocessCompress(InputStream in, int headerFormat)
			throws IOException {
		this.frequencies = new int[ALPH_SIZE + 1];
		this.headerFormat = headerFormat;
		// Frequency of the eof value is 1.
		frequencies[ALPH_SIZE] = 1;
		int preCompress = getFrequencies(
				new BitInputStream(new BufferedInputStream(in)));
		huffmanTree = new HuffmanTree(frequencies);
		codeMap = huffmanTree.createMap();
		// The magic number and header format are both integers, add their bits.
		int compress = BITS_PER_INT * 2;
		if (headerFormat == STORE_COUNTS) {
			// The standard count header takes up ALPH_SIZE * BITS_PER_INT bits.
			compress += ALPH_SIZE * BITS_PER_INT;
		} else if (headerFormat == STORE_TREE) {
			// The standard tree header takes up BITS_PER_INT for the size of
			// the tree as an integer and then the number of bits it takes to
			// store the data of the tree.
			compress += huffmanTree.getEncodeSize() + BITS_PER_INT;
		}
		compress += getCountOfData();
		bitsSaved = preCompress - compress;
		return bitsSaved;
	}

	// Reads the file and stores the frequency of each "word" in the frequency
	// array. Returns the number of "words" * BITS_PER_WORD.
	private int getFrequencies(InputStream in) throws IOException {
		BitInputStream bits = new BitInputStream(new BufferedInputStream(in));
		int total = 0;
		int inbits;
		while ((inbits = bits.readBits(BITS_PER_WORD)) != -1) {
			this.frequencies[inbits]++;
			total += BITS_PER_WORD;
		}
		bits.close();
		return total;
	}

	// Returns the number of bits it takes to store the compressed data.
	private int getCountOfData() {
		int total = 0;
		for (int i = 0; i < frequencies.length; i++) {
			String value = codeMap.get(i);
			// If the frequency is not 0, there will be a code for this "word".
			if (value != null) {
				// Frequency of the "word" * the length of its new code.
				total += frequencies[i] * value.length();
			}
		}
		return total;
	}

	/**
	 * Make sure this model communicates with some view.
	 * 
	 * @param viewer
	 *            is the view for communicating.
	 */
	public void setViewer(IHuffViewer viewer) {
		myViewer = viewer;
	}

	/**
	 * Uncompress a previously compressed stream in, writing the uncompressed
	 * bits/data to out.
	 * 
	 * @param in
	 *            is the previously compressed data (not a BitInputStream)
	 * @param out
	 *            is the uncompressed file/stream
	 * @return the number of bits written to the uncompressed file/stream
	 * @throws IOException
	 *             if an error occurs while reading from the input file or
	 *             writing to the output file.
	 */
	public int uncompress(InputStream in, OutputStream out) throws IOException {
		BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
		int magicNumber = bitIn.readBits(BITS_PER_INT);
		// Does not try to uncompress files that are not .hf files.
		if (magicNumber != MAGIC_NUMBER) {
			bitIn.close();
			return -1;
		}
		headerFormat = bitIn.readBits(BITS_PER_INT);
		HuffmanTree outTree;
		// Constructs the tree with the appropriate format. Does not try to
		// uncompress with unknown formats.
		if (headerFormat == STORE_COUNTS) {
			outTree = new HuffmanTree(inputCount(bitIn));
		} else if (headerFormat == STORE_TREE) {
			outTree = inputTree(bitIn);
		} else {
			bitIn.close();
			return -1;
		}
		BitOutputStream bitOut = new BitOutputStream(
				new BufferedOutputStream(out));
		// Writes the data and returns the number of bits written.
		return outTree.writeData(bitIn, bitOut);
	}

	// Creates a new frequency array using the standard count header.
	private int[] inputCount(BitInputStream bitIn) throws IOException {
		int[] inFreq = new int[ALPH_SIZE + 1];
		// There will be an EOF in the compressed data.
		inFreq[ALPH_SIZE] = 1;
		for (int i = 0; i < inFreq.length - 1; i++) {
			// Reads the frequency and adds the value to the array.
			int frequency = bitIn.readBits(BITS_PER_INT);
			inFreq[i] = frequency;
		}
		return inFreq;
	}

	// Creates a new HuffmanTree using the data in the standard tree header.
	private HuffmanTree inputTree(BitInputStream bitIn) throws IOException {
		int[] size = new int[]{bitIn.readBits(BITS_PER_INT) - 1};
		TreeNode outRoot = treeHelp(null, bitIn, size);
		return new HuffmanTree(outRoot);
	}

	// Creates and links together the nodes that will make up the new
	// HuffmanTree using the data in the standard tree header.
	private TreeNode treeHelp(TreeNode n, BitInputStream bitIn, int[] size)
			throws IOException {
		// There are still nodes to be added, also tells the method when to stop
		// reading.
		if (size[0] > 0) {
			size[0]--;
			int inBit = bitIn.readBits(1);
			// Does not have a "word" value, continue traversing tree and create
			// an internal node.
			if (inBit == 0) {
				TreeNode temp = new TreeNode(EMPTY_NODE, -1);
				temp.setLeft(treeHelp(temp.getLeft(), bitIn, size));
				temp.setRight(treeHelp(temp.getRight(), bitIn, size));
				return temp;
				// This is a leaf, create a new node and read in its value.
			} else {
				int value = bitIn.readBits(BITS_PER_WORD + 1);
				size[0] -= BITS_PER_WORD + 1;
				return new TreeNode(value, -1);
			}
		// Do not read anything, the tree is complete.
		} else
			return n;
	}

	// Displays a string in the HuffViewer.
	private void showString(String s) {
		if (myViewer != null)
			myViewer.update(s);
	}
}
