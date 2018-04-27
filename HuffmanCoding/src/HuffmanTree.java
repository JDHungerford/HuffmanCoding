import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class HuffmanTree {
    private TreeNode root;
    private int encodeSize;
    public HashMap<Integer, String> codeMap;

    public HuffmanTree(int[] frequencies){
        PriorityQ<TreeNode> queue = new PriorityQ<>();

        for (int i = 0; i < frequencies.length; i++) {
            if (frequencies[i] > 0) {
                TreeNode temp = new TreeNode(i, frequencies[i]);
                queue.queue(temp);
                encodeSize += 10;
            }
        }
        //creating the tree
        while (queue.size() >= 2) {
            //make a new node with the second left and right nodes from the
            //front of the list
            TreeNode left = queue.dequeue();
            TreeNode right = queue.dequeue();
            TreeNode temp = new TreeNode(left, -1, right);
            encodeSize++;
            queue.queue(temp);
        }
        root = queue.dequeue();
        //printTree(root, " ");
        createMap();

    }

    public HuffmanTree(TreeNode root){
        this.root = root;
        createMap();
    }

    //returns an encoded map resulting from the HuffmanTree
    public void createMap(){
        if (root == null){
            throw new IllegalStateException("Tree is empty");
        }
        codeMap = new HashMap<Integer, String>();
        encode(codeMap, "", root);
    }

    public int getEncodeSize(){
        return encodeSize;
    }

    public int writeData(BitInputStream in, BitOutputStream out)throws IOException{
        int[] bitCounts = new int[1];
        writeHelper(root, in, out, bitCounts);
        in.close();
        out.flush();
        out.close();
        return bitCounts[0];
    }

    private void writeHelper(TreeNode n, BitInputStream in, BitOutputStream out, int[] current)throws IOException{
        int bit = in.readBits(IHuffConstants.BITS_PER_WORD);
        if (bit != IHuffConstants.PSEUDO_EOF){
            if (n.isLeaf()){
                current[0]++;
                out.writeBits(IHuffConstants.BITS_PER_WORD, n.getValue());
            }else if (bit == 0){
                current[0]++;
                writeHelper(n.getLeft(), in, out, current);
            }else{
                current[0]++;
                writeHelper(n.getRight(), in, out, current);
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

    private void printTree(TreeNode n, String spaces) {
        if(n != null){
            printTree(n.getRight(), spaces + "  ");
            System.out.println(spaces + "[ " + n.getValue() + " , " + n.getFrequency() + "]");
            printTree(n.getLeft(), spaces + "  ");
        }
    }
}
