package qbfsolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DisjunctionDefault implements Disjunction {
	private Set<Integer> st;
	private boolean satisfied;
	public DisjunctionDefault() {
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
	
	public List<Integer> getLiteral() {
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
	/**
	 * 
	 * @param v varible_id always positive
	 * @param val boolean value, 0 or 1
	 * @param f, the formula which this clause is set
	 * @param id, clause id in the formula f
	 */
	public void set(int w, DataStructureOptimizedFormula f, int id) {
		// st, set of literals
		if (st.contains(w)) {
			this.satisfied = true;
			clear(f);
			f.removecnf(id);
		} 
		
		if (st.contains(-w)) {
			deletevar(-w, f);
			if (st.size() == 1) {
				addUnitClause(st.iterator().next(), f);
			} else if (st.size() == 2) {
				for (Integer v : st) {
					f.updatebinary(v, 1);
				}
			}
			// check it later
			if (evaluate() == 0) {
				f.setSatisfied(false);
			}
		}
	}
	
	private void deletevar(int v, DataStructureOptimizedFormula f) {
		st.remove(v);
		updatecounter(v, -1, f);
	}
	
	private void clear(DataStructureOptimizedFormula f) {
		for (Integer it : this.st) {
			updatecounter(it, -1, f);
			if (st.size() == 2) {
				f.updatebinary(it, -1);
			}
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

	public DisjunctionDefault duplidate() {
		DisjunctionDefault c = new DisjunctionDefault();
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
	
	public void set(int v, PersistentFormula f, int val, int id) {
		return;
	}

	@Override
	public List<Integer> getAll() {
		return this.getVariable();
	}

}
