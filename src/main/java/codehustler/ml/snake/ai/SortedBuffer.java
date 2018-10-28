package codehustler.ml.snake.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * 
 * A buffer which keeps only the top n elements
 * 
 * used for Hall Of Fame leaderboard
 * 
 * @author daniel.mueller
 *
 */
public class SortedBuffer<E extends Comparable<E>> {

	private final int capacity;
	
	private final List<E> elements;
	

	public SortedBuffer(int capacity) {
		this.capacity = capacity;
		this.elements = new ArrayList<>();
	}
	
	public void addAll(Collection<E> elements) {
		this.elements.removeAll(elements);
		this.elements.addAll(elements);
		this.elements.sort(Comparator.reverseOrder());
		this.elements.retainAll(this.elements.subList(0, capacity-1));
	}

	public boolean add(E element) {
		elements.remove(element);
		elements.add(element);
		elements.sort(Comparator.reverseOrder()); //d'oh
		elements.retainAll(elements.subList(0, capacity-1));
		return elements.contains(element);
	}

	public List<E> get() {
		return new ArrayList<>(elements);
	}
}
