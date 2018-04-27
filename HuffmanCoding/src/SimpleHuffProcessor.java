/*  Student information for assignment:
 *
 *  On <MY|OUR> honor, Jacob Hungerford and Miles Chandler, this programming assignment is <MY|OUR> own work
 *  and <I|WE> have not provided this code to any other student.
 *
 *  Number of slip days used: 0
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

public class SimpleHuffProcessor implements IHuffProcessor {
	
	final static int EMPTY_NODE = -1;
	
    private IHuffViewer myViewer;
    private HuffmanTree huffmanTree;
    private HashMap<Integer, String> codeMap;
    private int bitsSaved;
    private int headerFormat;
    private int[] frequencies;

    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        int compressedBits = 0;
        if (bitsSaved > 0 || force) {
            BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
            BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
            String eof = codeMap.get(PSEUDO_EOF);
            compressedBits += BITS_PER_INT * 2 + eof.length();
            bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            bitOut.writeBits(BITS_PER_INT, headerFormat);
            if (headerFormat == STORE_COUNTS){
                compressedBits += countHeader(bitOut);
            } else if (headerFormat == STORE_TREE) {
                compressedBits += treeHeader(bitOut);
            } else {
            	bitIn.close();
            	bitOut.close();
            	return -1;
            }
            compressedBits += compressedData(bitOut, bitIn);
            bitOut.writeBits(eof.length(), Integer.valueOf(eof, 2));
            bitOut.close();
        }
        return compressedBits;
    }

    private int countHeader(BitOutputStream bitOut){
        for (int i = 0; i < ALPH_SIZE; i++){
            bitOut.writeBits(BITS_PER_INT, frequencies[i]);
        }
        return ALPH_SIZE * BITS_PER_INT;
    }

    private int treeHeader(BitOutputStream bitOut){
        bitOut.writeBits(BITS_PER_INT, huffmanTree.getEncodeSize());
        int bits = BITS_PER_INT;
        for (TreeNode node : huffmanTree.getNodeList()){
            int value = node.getValue();
            bits++;
            if (value == EMPTY_NODE) {
            	bitOut.writeBits(1, 0);
            } else {
            	bitOut.writeBits(1, 1);
            	//then write the value of the node
            	bitOut.writeBits(BITS_PER_WORD + 1, value);
            	bits += BITS_PER_WORD + 1;
            }
        }
        return bits;
    }

    private int compressedData(BitOutputStream bitOut, BitInputStream bitIn) throws IOException{
        int inBits;
        int bits = 0;
        while ((inBits = bitIn.readBits(BITS_PER_WORD)) != -1){
            String code = codeMap.get(inBits);
            int length = code.length();
            bitOut.writeBits(length, Integer.valueOf(code, 2));
            bits += length;
        }
        bitIn.close();
        return bits;
    }

    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        this.frequencies = new int[ALPH_SIZE + 1];
        this.headerFormat = headerFormat;
        frequencies[ALPH_SIZE] = 1;
        int preCompress = getFrequencies(new BitInputStream(new BufferedInputStream(in)));
        huffmanTree = new HuffmanTree(frequencies);
        codeMap = huffmanTree.createMap();
        int compress = BITS_PER_INT * 2; //start at 32 because of the magic number is an int
        //plus another int for the header format
        //if the headerFormat is stre counts
        //increment by the alpsize times bits per int
        if (headerFormat == STORE_COUNTS){
            compress += ALPH_SIZE * BITS_PER_INT;
        }else if (headerFormat == STORE_TREE){
            //if the headerFormat is store tree
            //get number from huffman class
            compress += huffmanTree.getEncodeSize() + BITS_PER_INT;
            //we add 32 bits to the total because store tree
            //requires us to store the size of the tree
        }
        compress += getCountOfData();
        bitsSaved = preCompress - compress;
        return bitsSaved;
    }
    
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

    private int getCountOfData(){
        int total = 0;
        for (int i = 0; i < frequencies.length; i++) {
        	String value = codeMap.get(i);
        	if (value  != null) {
        		total += frequencies[i] * value.length();
        	}
        }
        return total;
    }

    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    public int uncompress(InputStream in, OutputStream out) throws IOException {
        int uncompressedBits = 0;
        BitInputStream bitIn = new BitInputStream(new BufferedInputStream(in));
        int magicNumber = bitIn.readBits(BITS_PER_INT);
        if (magicNumber != MAGIC_NUMBER){
            bitIn.close();
            return -1;
        }
        headerFormat = bitIn.readBits(BITS_PER_INT);
        HuffmanTree outTree;
        if (headerFormat == STORE_COUNTS){
            outTree = new HuffmanTree(inputCount(bitIn));
        }else if (headerFormat == STORE_TREE){
            outTree = inputTree(bitIn);
        }else{
            bitIn.close();
            return -1;
        }
        BitOutputStream bitOut = new BitOutputStream(new BufferedOutputStream(out));
        uncompressedBits += outTree.writeData(bitIn, bitOut);
        return uncompressedBits;
    }
    
    private int[] inputCount(BitInputStream bitIn) throws IOException{
        int[] inFreq = new int[ALPH_SIZE + 1];
        inFreq[256]++;
        for (int i = 0; i < inFreq.length - 1; i++){
            int frequency = bitIn.readBits(BITS_PER_INT);
            inFreq[i] = frequency;
        }
        return inFreq;
    }

    private HuffmanTree inputTree(BitInputStream bitIn) throws IOException{
        int[] size = new int[] {bitIn.readBits(BITS_PER_INT) - 1};
        TreeNode outRoot = treeHelp(null, bitIn, size);
        return new HuffmanTree(outRoot);
    }

    private TreeNode treeHelp(TreeNode n, BitInputStream bitIn, int[] size)throws IOException{
    	if (size[0] > 0) {
    		size[0]--;
	    	int inBit = bitIn.readBits(1);
	    	if (inBit == 0) {
	    		TreeNode temp = new TreeNode(EMPTY_NODE, -1);
	    		temp.setLeft(treeHelp(temp.getLeft(), bitIn, size));
	    		temp.setRight(treeHelp(temp.getRight(), bitIn, size));
	    		return temp;
	    	} else {
	    		int value = bitIn.readBits(BITS_PER_WORD + 1);
	    		size[0] -= BITS_PER_WORD + 1;
	    		return new TreeNode(value, -1);
	    	}
    	} else
    		return n;
    }

    private void showString(String s){
        if(myViewer != null)
            myViewer.update(s);
    }
}
