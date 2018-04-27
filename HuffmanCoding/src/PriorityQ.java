import java.util.ArrayList;

public class PriorityQ<E extends Comparable<? super E>> {
	
	private ArrayList<E> con;
	
	public PriorityQ() {
		con = new ArrayList<E>();
	}
	
	public void queue(E item) {
		int index = 0;
		while (index < con.size() && item.compareTo(con.get(index)) < 0)
			index++;
		con.add(index, item);
	}
	
	public E dequeue() {
		if (size() == 0)
			throw new IllegalStateException("The queue is empty.");
		return con.remove(size() - 1);
	}
	
	public int size() {
		return con.size();
	}
	
	public String toString() {
		return con.toString();
	}
}
