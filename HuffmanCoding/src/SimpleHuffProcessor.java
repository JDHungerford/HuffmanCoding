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
        PriorityQ<TreeNode> queue = new PriorityQ<>();
        for (int i = 0; i <= ALPH_SIZE; i++) {
        	if (frequencies[i] > 0) {
        		TreeNode temp = new TreeNode(i, frequencies[i]);
        		queue.queue(temp);
        	}
        }
        //creating the tree
        while (queue.size() >= 2) {
            //make a new node with the second left and right nodes from the
            //front of the list
        	TreeNode temp = new TreeNode(queue.dequeue(), 0, queue.dequeue());
        	queue.queue(temp);
        }
        TreeNode huffmanNode = queue.dequeue();
        huffmanTree = new HuffmanTree(huffmanNode);
        System.out.println(queue.size());
        System.out.println(huffmanNode.getFrequency());
        return preCompress;
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
    	int total = 1;
        int inbits;
        while ((inbits = bits.readBits(BITS_PER_WORD)) != -1) {
        	frequencies[inbits]++;
        	total++;
        }
        bits.close();
        return total;
    }
}
