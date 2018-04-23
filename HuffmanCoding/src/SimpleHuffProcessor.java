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

    public int compress(InputStream in, OutputStream out, boolean force) throws IOException {
        throw new IOException("compress is not implemented");
        //return 0;
    }

    public int preprocessCompress(InputStream in, int headerFormat) throws IOException {
        int[] frequencies = new int[ALPH_SIZE + 1];
        frequencies[ALPH_SIZE] = 1;
        int preCompress = getFrequencies(new BitInputStream(in), frequencies);
        System.out.println(preCompress);
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
        compress += getCountOfData(frequencies);
        return preCompress - compress;
    }

    private int getCountOfData(int[] frequencies){
        int total = 0;
        for (int i : frequencies){
            if (huffmanTree.codeMap.get(i) != null)
                total += i * huffmanTree.codeMap.get(i).length();
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
    
    private int getFrequencies(BitInputStream bits, int[] frequencies) throws IOException {
    	int total = 9;
        int inbits;
        while ((inbits = bits.readBits(BITS_PER_WORD)) != -1) {
        	frequencies[inbits]++;
        	total += 8;
        }
        bits.close();
        return total;
    }
}
