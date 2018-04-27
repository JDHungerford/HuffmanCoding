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

import java.util.ArrayList;

// A class used to model a priority queue in which ties are broken fairly.
// Items are removed from the end of the internal storage container and 
// new items are placed behind items with indentical compareTo values.
public class PriorityQ<E extends Comparable<? super E>> {

	private ArrayList<E> con;

	// Creates a new PriorityQ object, initializes its internal storage
	// container.
	public PriorityQ() {
		con = new ArrayList<E>();
	}

	// Adds an item to the PriorityQ. Breaks ties between items fairly. O(N)
	public void queue(E item) {
		int index = 0;
		while (index < con.size() && item.compareTo(con.get(index)) < 0)
			index++;
		con.add(index, item);
	}

	// Removes an item from the front of the PriorityQ. O(1)
	public E dequeue() {
		if (size() == 0)
			throw new IllegalStateException("The queue is empty.");
		return con.remove(size() - 1);
	}

	// Returns the size of the PriorityQ. O(1)
	public int size() {
		return con.size();
	}

	// Returns the contents of the PriorityQ in String form. Used for debugging
	// purposes. O(N)
	public String toString() {
		return con.toString();
	}
}
