import java.util.ArrayList;

public class PriorityQ<E extends Comparable<? super E>> {
	
	private ArrayList<E> con;
	private int size;
	
	public PriorityQ() {
		con = new ArrayList<E>();
	}
	
	public void queue(E item) {
		con.add(binSearch(item, 0, size - 1), item);
		size++;
	}
	
	public E dequeue() {
		if (size == 0)
			throw new IllegalStateException("The queue is empty.");
		size--;
		return con.remove(size);
	}
	
	public int size() {
		return size;
	}
	
	private int binSearch(E target, int low, int high) {
		if (low <= high) {
			int mid = low + ((high - low) / 2);
			if (con.get(mid).equals(target))
				return mid;
			else if (con.get(mid).compareTo(target) > 0)
				return binSearch(target, low, mid - 1);
			else
				return binSearch(target, mid + 1, high);
		}
		return low;
	}
}
