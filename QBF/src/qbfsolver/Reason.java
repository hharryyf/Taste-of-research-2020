package qbfsolver;

import java.util.LinkedList;
import java.util.TreeSet;

public class Reason {
	private boolean type;
	public TreeSet<Integer> assign;
	public Reason(boolean type) {
		this.type = type;
		this.assign = new TreeSet<>();
    }
	
	public void init(PersistentFormula f) {
		if (type) {
			LinkedList<Pair<Integer, Character>> list = f.getAssignment();
			for (Pair<Integer, Character> curr : list) {
				if (curr.second != 'B' && !f.isMax(curr.first)) {
					this.assign.add(curr.first);
				}
			}
		}
	}
	
	public Reason calcNew(Reason other, int v) {
		Reason ret = new Reason(type);
		if (type) {
			ret.assign = new TreeSet<>(other.assign);
			ret.assign.addAll(this.assign);
			ret.assign.remove(v);
			ret.assign.remove(-v);
		}
		
		return ret;
	}
}
