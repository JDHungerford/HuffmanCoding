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
import java.util.ArrayList;

public class SimpleHuffProcessor implements IHuffProcessor {

    private IHuffViewer myViewer;
    private HuffmanTree huffmanTree;
    private int bitsSaved;
    private int headerFormat;
    private int[] frequencies;

    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        if (bitsSaved > 0 || force) {
            BitInputStream bitInt = new BitInputStream(in);
            BitOutputStream bitOut = new BitOutputStream(out);
            bitOut.writeBits(BITS_PER_INT, MAGIC_NUMBER);
            bitOut.writeBits(BITS_PER_INT, headerFormat);
            if (headerFormat == STORE_COUNTS){
                for (int i = 0; i < ALPH_SIZE; i++){
                    if (huffmanTree.codeMap.get(i) != null)
                        bitOut.writeBits(BITS_PER_INT, frequencies[i]);
                }
            } else if (headerFormat == STORE_TREE) {
                bitOut.writeBits(BITS_PER_INT, huffmanTree.getEncodeSize());
                Integer.to
            }
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
