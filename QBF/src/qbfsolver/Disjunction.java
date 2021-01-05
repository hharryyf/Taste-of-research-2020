package qbfsolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Disjunction {
	private Set<Integer> st;
	private boolean satisfied;
	public Disjunction() {
		this.st = new HashSet<Integer>();
		this.satisfied = false;
	}
	
	public boolean hasvar(int val) {
		return st.contains(val) || st.contains(-val);
	}
	
	public boolean contains(int val) {
		return st.contains(val);
	}
	
	public boolean isEmpty() {
		return this.st.isEmpty();
	}
	
	public List<Integer> getSt() {
		List<Integer> list = new ArrayList<Integer>();
		Iterator<Integer> it = st.iterator();
	    while(it.hasNext()){
	        list.add(Math.abs(it.next()));
	    }
		return list;
	}
	
	public List<Integer> getVariable() {
		List<Integer> list = new ArrayList<Integer>();
		Iterator<Integer> it = st.iterator();
	    while(it.hasNext()){
	        list.add(it.next());
	    }
		return list;
	}
	
	public int getSize() {
		return this.st.size();
	}
	
	public void add(int val) {
		// if the set contains both val and -val
		// then it is considered as satisfied already
		if (this.satisfied) return;
		if (st.contains(-val)) {
			this.satisfied = true;
			st.clear();
		}
		st.add(val);
	}
	
	// set v to be value
	public void set(int v, int val) {
		if (st.contains(v)) {
			if (val == 1) {
				this.satisfied = true;
			} else {
				st.remove(v);
			}
		} 
		
		if (st.contains(-v)) {
			if (val == 1) {
				st.remove(-v);
			} else {
				this.satisfied = true;
			}
		}
	}
	
	public void set(int v, int val, DataStructureOptimizedFormula f, int id) {
		if (st.contains(v)) {
			if (val == 1) {
				this.satisfied = true;
				clear(f);
			} else {
				deletevar(v, f);
			}
		} 
		
		if (st.contains(-v)) {
			if (val == 1) {
				deletevar(-v, f);
			} else {
				this.satisfied = true;
				clear(f);
			}
		}
		
		if (st.size() == 1) {
			addUnitClause(st.iterator().next(), f);
		}
		
		if (evaluate() == 0) {
			f.setSatisfied(false);
		} else if (evaluate() == 1) {
			f.removecnf(id);
		}
	}
	
	private void deletevar(int v, DataStructureOptimizedFormula f) {
		st.remove(v);
		updatecounter(v, -1, f);
	}
	
	private void clear(DataStructureOptimizedFormula f) {
		for (Integer it : this.st) {
			updatecounter(it, -1, f);
		}
		st.clear();
	}
	
	
	private void updatecounter(int v, int inc, DataStructureOptimizedFormula f) {
		f.updateCounter(v, inc);
	}
	
	private void addUnitClause(int v, DataStructureOptimizedFormula f) {
		f.addUnitVariable(v);
	}
	
	public int evaluate() {
		if (this.satisfied) return 1;
		if (this.st.isEmpty()) return 0;
		return -1;
	}

	public Disjunction duplidate() {
		Disjunction c = new Disjunction();
		Iterator<Integer> it = st.iterator();
	    while(it.hasNext()){
	        c.add(it.next());
	    }
		c.satisfied = this.satisfied;
		return c;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (this.satisfied) {
			sb.append("true ");
		}
		
		Iterator<Integer> it = st.iterator();
	    while(it.hasNext()){
	    	if (sb.length() == 0) {
	    		sb.append(it.next());
	        } else {
	        	sb.append(" V " + it.next());
	        }
	    }
	    
	    return sb.toString();
	}
}
