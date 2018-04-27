import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HuffmanTree implements IHuffConstants{
    private TreeNode root;
    private int encodeSize;

    public HuffmanTree(int[] frequencies){
        PriorityQ<TreeNode> queue = new PriorityQ<>();
        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                TreeNode temp = new TreeNode(i, frequencies[i]);
                queue.queue(temp);
                encodeSize += BITS_PER_WORD + 2;
            }
        }
        //creating the tree
        while (queue.size() > 1) {
            //make a new node with the second left and right nodes from the
            //front of the list
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            TreeNode temp = new TreeNode(left, SimpleHuffProcessor.EMPTY_NODE, right);
            encodeSize++;
            queue.queue(temp);
        }
        root = queue.dequeue();
        //printTree(root, " ");
    }

    public HuffmanTree(TreeNode root){
        this.root = root;
    }

    //returns an encoded map resulting from the HuffmanTree
    public HashMap<Integer, String> createMap(){
        if (root == null){
            throw new IllegalStateException("Tree is empty");
        }
        HashMap<Integer, String> codeMap = new HashMap<>();
        encode(codeMap, "", root);
        return codeMap;
    }

    public int getEncodeSize(){
        return encodeSize;
    }

    public int writeData(BitInputStream in, BitOutputStream out)throws IOException{
    	int bitCount = 0;
        int value;
        while ((value = writeHelper(root, in, out)) != PSEUDO_EOF) {
        	out.writeBits(BITS_PER_WORD, value);
        	bitCount += BITS_PER_WORD;
        }
        in.close();
        out.close();
        return bitCount;
    }

    private int writeHelper(TreeNode n, BitInputStream in, BitOutputStream out)throws IOException{
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

    //helper method that encodes nodes into a tree map with its corresponding
    //path in the form of a string of 1 and 0
    private void encode(HashMap<Integer, String> codeMap, String path, TreeNode n){
        //if the node is a leaf we can now put the value and path into the map
        if (n.isLeaf()){
            codeMap.put(n.getValue(), path);
        }else{
            //not a leaf
            //go left and concat a 0 to the path
            if (n.getLeft() != null){
                encode(codeMap, path + "0", n.getLeft());
            }
            //go right and concat a 1 to the path
            if (n.getRight() != null){
                encode(codeMap, path + "1", n.getRight());
            }
        }
    }

    //method returns a list of the tree nodes in preOrder for compression
    //in the SimpleHuffProcessor class
    public ArrayList<TreeNode> getNodeList(){
        ArrayList<TreeNode> list = new ArrayList<>();
        getListHelper(root, list);
        return list;
    }

    //recursive helper method that traverses the tree and adds nodes
    //to a list in pre order
    private void getListHelper(TreeNode n, ArrayList<TreeNode> list){
        if (n != null){
            list.add(n);
            getListHelper(n.getLeft(), list);
            getListHelper(n.getRight(), list);
        }
    }

    public void printTree() {
    	for (TreeNode node : getNodeList())
    		System.out.println(node);
    }
}
