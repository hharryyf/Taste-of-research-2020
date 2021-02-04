package qbfsolver;

public class Pair<K extends Comparable<K>, V extends Comparable<V>> implements Comparable<Pair<K, V>>{
	private K first;
	private V second;
	public Pair(K first, V second) {
		this.first = first;
		this.second = second;
	}
	
	public K getFirst() {
		return this.first;
	}
	
	public V getSecond() {
		return this.second;
	}
	
	public void setFirst(K first) {
		this.first = first;
	}
	
	public void setSecond(V second) {
		this.second = second;
	}

	@Override
	public int compareTo(Pair<K, V> o) {
		if (this.getFirst().compareTo(o.getFirst()) != 0) {
			return this.getFirst().compareTo(o.getFirst());
		}
		return this.getSecond().compareTo(o.getSecond());
	}
	
	@Override
	public String toString() {
		return "(" + getFirst().toString() + " ," + getSecond().toString() + ")";
	}
}
