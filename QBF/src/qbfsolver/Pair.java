package qbfsolver;

public class Pair<K extends Object, V extends Object> {
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
		public String toString() {
			return "(" + getFirst().toString() + " ," + getSecond().toString() + ")";
		}
}

