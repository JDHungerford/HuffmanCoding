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
 *  UTEID:
 *  email address:
 *
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private HuffmanTree huffmanTree;
    private int bitsSaved;
    private int headerFormat;
    private int[] frequencies;

    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (bitsSaved > 0 || force) {
            BitInputStream bitIn = new BitInputStream(in);
            BitOutputStream bitOut = new BitOutputStream(out);
            //write the magic number
            bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            //write the header format
            bitOut.writeBits(BITS_PER_INT, headerFormat);
            //write the storeCount header
            if (headerFormat == STORE_COUNTS){
                outputStdCount(bitOut);
            } else if (headerFormat == STORE_TREE) {
                //else write the stdTree header
                outputStdTree(bitOut);
            }
            outputData(bitOut, bitIn);
            bitIn.close();
            bitOut.close();
        }
        return 69;
    }

    private void outputStdCount(BitOutputStream bitOut){
        for (int i = 0; i < ALPH_SIZE; i++){
            //if (huffmanTree.codeMap.get(i) != null)//TODO get rid of this line
            bitOut.writeBits(BITS_PER_INT, frequencies[i]);
        }
    }

    private void outputStdTree(BitOutputStream bitOut){
        bitOut.writeBits(BITS_PER_INT, huffmanTree.getEncodeSize());
        for (TreeNode tNode : huffmanTree.getNodeList()){
            int tValue = tNode.getValue();
            //if the node is not a leaf
            if (tValue == -1)//TODO possible final constant
                //write a 0
                bitOut.writeBits(1, 0);
            else {//if the node is a leaf
                //write a 1
                bitOut.writeBits(1, 1);
                //then write the value of the node
                bitOut.writeBits(BITS_PER_INT + 1, tValue);
            }

        }
    }

    private void outputData(BitOutputStream bitOut, BitInputStream bitIn) throws IOException{
        int inBits = 0;
        while ((inBits = bitIn.readBits(BITS_PER_WORD)) != -1){
            String code = huffmanTree.codeMap.get(inBits);
            bitOut.writeBits(BITS_PER_INT, Integer.valueOf(code, 2));
        }
    }

    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        this.frequencies = new int[ALPH_SIZE + 1];
        this.headerFormat = headerFormat;
        frequencies[ALPH_SIZE] = 1;
        int preCompress = getFrequencies(new BitInputStream(in));
        huffmanTree = new HuffmanTree(frequencies);
        int compress = BITS_PER_INT * 2; //start at 32 because of the magic number is an int
        //plus another int for the header format
        //if the headerFormat is stre counts
        //increment by the alpsize times bits per int
        if (headerFormat == STORE_COUNTS){
            compress += ALPH_SIZE * BITS_PER_INT;
        }else if (headerFormat == STORE_TREE){
            //if the headerFormat is store tree
            //get number from huffman class
            compress += huffmanTree.getEncodeSize() + 32;
            //we add 32 bits to the total because store tree
            //requires us to store the size of the tree
        }
        compress += getCountOfData();
        bitsSaved = preCompress - compress;

        return bitsSaved;
    }

    private int getCountOfData(){
        int total = 0;
        for (int i = 0; i <= ALPH_SIZE; i++){
            if (huffmanTree.codeMap.get(i) != null)
                total += this.frequencies[i] * huffmanTree.codeMap.get(i).length();
        }
        return total;
    }



    public void setViewer(IHuffViewer viewer) {
        myViewer = viewer;
    }

    public int uncompress(InputStream in, OutputStream out) throws IOException {
        throw new IOException("uncompress not implemented");
        //return 0;
    }

    private void showString(String s){
        if(myViewer != null)
            myViewer.update(s);
    }
    
    private int getFrequencies(BitInputStream bits) throws IOException {
        int total = 0;
        int inbits;
        while ((inbits = bits.readBits(BITS_PER_WORD)) != -1) {
        	this.frequencies[inbits]++;
        	total += BITS_PER_WORD;
        }
        bits.close();
        return total;
    }
}
