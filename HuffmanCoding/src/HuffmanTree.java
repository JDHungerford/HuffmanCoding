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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

// A class used to model the tree in the Huffman compression algorithm.
public class HuffmanTree implements IHuffConstants {

	private TreeNode root;
	private int encodeSize;

	// Creates a new HuffmanTree using the frequency array.
	public HuffmanTree(int[] frequencies) {
		PriorityQ<TreeNode> queue = new PriorityQ<>();
		for (int i = 0; i < frequencies.length; i++) {
			// Loops through the array and creates nodes for "words" with a
			// frequency greater than 0.
			if (frequencies[i] > 0) {
				TreeNode temp = new TreeNode(i, frequencies[i]);
				queue.queue(temp);
				// Each node takes up one bit to indicate whether it has a
				// value or not, and then BITS_PER_WORD + 1 more bits to
				// account for the original ALPH_SIZE with the addition of the
				// eof.
				encodeSize += BITS_PER_WORD + 2;
			}
		}
		// While there are atleast two nodes in the queue, remove the first two
		// nodes and create a new node linked to these two nodes with their
		// combined frequencies.
		while (queue.size() > 1) {
			TreeNode left = queue.dequeue();
			TreeNode right = queue.dequeue();
			TreeNode temp = new TreeNode(left, SimpleHuffProcessor.EMPTY_NODE,
					right);
			// Each new node requires 1 bit to indicate it does not contain a
			// value.
			encodeSize++;
			queue.queue(temp);
		}
		// The root is the only node in the queue once it is finished.
		root = queue.dequeue();
	}

	// Creates a new HuffmanTree using a pre-existing root.
	public HuffmanTree(TreeNode root) {
		this.root = root;
	}

	// Returns an a map of the new codes by traversing the tree.
	public HashMap<Integer, String> createMap() {
		if (root == null) {
			throw new IllegalStateException("Tree is empty");
		}
		HashMap<Integer, String> codeMap = new HashMap<>();
		encode(codeMap, "", root);
		return codeMap;
	}

	// Helper method that creates the new codes for each leaf in the tree and
	// adds it to the codeMap.
	private void encode(HashMap<Integer, String> codeMap, String path,
			TreeNode n) {
		// if the node is a leaf we can now put the value and path into the map
		if (n.isLeaf()) {
			codeMap.put(n.getValue(), path);
		} else {
			// not a leaf
			// go left and concat a 0 to the path
			if (n.getLeft() != null) {
				encode(codeMap, path + "0", n.getLeft());
			}
			// go right and concat a 1 to the path
			if (n.getRight() != null) {
				encode(codeMap, path + "1", n.getRight());
			}
		}
	}

	// Returns the encode size of the tree.
	public int getEncodeSize() {
		return encodeSize;
	}

	// Writes out the uncompressed form of the data by traversing the tree and
	// writing the values indicated by the compressed codes.
	// Returns the number of bits written.
	public int writeData(BitInputStream in, BitOutputStream out)
			throws IOException {
		int bitCount = 0;
		int value;
		// Continues reading and writing until the PSEUDO_EOF is reached.
		while ((value = writeHelper(root, in, out)) != PSEUDO_EOF) {
			out.writeBits(BITS_PER_WORD, value);
			bitCount += BITS_PER_WORD;
		}
		in.close();
		out.close();
		return bitCount;
	}

	// Helper method for writeData. Traverses the tree by reading in the
	// compressed data and returns the value indicated by the leaf reached.
	private int writeHelper(TreeNode n, BitInputStream in, BitOutputStream out)
			throws IOException {
		if (n.isLeaf()) {
			return n.getValue();
		} else {
			int bitIn = in.readBits(1);
			if (bitIn == 0) {
				return writeHelper(n.getLeft(), in, out);
			} else {
				return writeHelper(n.getRight(), in, out);
			}
		}
	}

	// Returns a list of the nodes in the tree with a pre-order traversal.
	public ArrayList<TreeNode> getNodeList() {
		ArrayList<TreeNode> list = new ArrayList<>();
		getListHelper(root, list);
		return list;
	}

	// Helper method for getNodeList. Adds the current node to the list, then
	// adds the left and right children if they exist.
	private void getListHelper(TreeNode n, ArrayList<TreeNode> list) {
		if (n != null) {
			list.add(n);
			getListHelper(n.getLeft(), list);
			getListHelper(n.getRight(), list);
		}
	}

	// Prints out the nodes of the tree in pre-order. Used for debugging
	// purposes.
	public void printTree() {
		for (TreeNode node : getNodeList())
			System.out.println(node);
	}
}
