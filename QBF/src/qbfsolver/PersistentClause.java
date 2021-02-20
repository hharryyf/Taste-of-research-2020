package qbfsolver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class PersistentClause implements Disjunction {
	
	private int unicount = 0, existcount = 0;
	private int proved = 0, disproved = 0;
	// [literal, value (-1, 0, 1), 0 means -literal, 1 means good, -1 means pending]
	private Map<Integer, Integer> literal; 
	
	public PersistentClause() {
		literal = new HashMap<>();
	}
 	
	@Override
	public boolean hasvar(int val) {
		return this.literal.containsKey(val) || this.literal.containsKey(-val);
	}

	@Override
	public boolean contains(int val) {
		return this.literal.containsKey(val);
	}

	@Override
	public boolean isEmpty() {
		return this.literal.isEmpty();
	}

	@Override
	public List<Integer> getLiteral() {
		List<Integer> ret = new ArrayList<>();
		Iterator<Entry<Integer, Integer>> itr = literal.entrySet().iterator(); 
		while (itr.hasNext()) {
			Entry<Integer, Integer> entry = itr.next();
			if (entry.getValue() == -1) {
				ret.add(entry.getKey());
			}
		}
		return ret;
	}

	@Override
	public List<Integer> getVariable() {
		List<Integer> ret = new ArrayList<>();
		Iterator<Entry<Integer, Integer>> itr = literal.entrySet().iterator(); 
		while (itr.hasNext()) {
			Entry<Integer, Integer> entry = itr.next();
			if (entry.getValue() == -1) {
				ret.add(Math.abs(entry.getKey()));
			}
		}
		return ret;
	}

	@Override
	public int getSize() {
		return this.literal.size();
	}

	@Override
	public void add(int val) {
		return;
	}
	
	public void add(int val, PersistentFormula f) {
		if (this.literal.containsKey(-val)) {
			this.proved += 2;
		}
		
		if (f.isMax(val)) {
			this.incExist(1);
		} else {
			this.incUni(1);
		}
		this.literal.put(val, -1);
	}
	
	private void incExist(int val) {
		this.existcount += val;
	}
	
	private void incUni(int val) {
		this.unicount += val;
	}
	
	@Override
	public void set(int v, int val) {
		return;
	}

	@Override
	public void set(int w, DataStructureOptimizedFormula f, int id) {
		return;
	}
	
	
	/**
	 *
	 * if v is negative then we print an error message
	 * 
	 * @param v variable id (> 0)
	 * @param f formula reference
	 * @param val (-1, 0, 1), note that there's never assignment from 1->0 or 0->1
	 * only 1->-1, -1->1, 0->-1,-1->0 possible
	 */
	public void set(int v, PersistentFormula f, int val) {
		int beforeeval = evaluate();
		if (v < 0) {
			System.err.print("something bad has happened");
			System.exit(0);
		}
		
		if (!literal.containsKey(v) && !literal.containsKey(-v)) {
			System.err.print("adjacency list is bad");
			System.exit(0);
		}
		
		if (val == -1) {
			int w = literal.getOrDefault(v, 0) + literal.getOrDefault(-v, 0);
			if (w == -1) return;
			int u = literal.containsKey(v) ? v : -v;
			if (w == 0) {
				this.disproved--;
				literal.put(u, -1);
			} else {
				this.proved--;
				literal.put(u, -1);
			}
			updateCounter(u, 1, f);
		} else {
			int w = v * (val == 1 ? 1 : -1), u = literal.containsKey(v) ? v : -v;
			if (w == u) {
				literal.put(u, 1);
				this.proved++;
			} else {
				literal.put(u, 0);
				this.disproved++;
			}
			
			updateCounter(u, -1, f);
		}
		
			
		int aftereval = evaluate();
		if (aftereval != beforeeval) {
			change(beforeeval, aftereval, f);
		}
	}
	
	public int getUni() {
		return this.unicount;
	}
	
	@Override
	public int evaluate() {
		if (this.proved > 0) return 1;
		if (this.disproved == this.literal.size()) return 0;
		if (this.existcount == 0) return 0;
		return -1;
	}

	@Override
	public Disjunction duplidate() {
		return this;
	}
	
	private void updateCounter(int v, int inc, PersistentFormula f) {
		if (f.isMax(v)) {
			this.incExist(inc);
		} else {
			this.incUni(inc);
		}
		
		if (inc == -1 && this.existcount == 1 && f.isMax(v)) {
			Iterator<Entry<Integer, Integer>> itr = literal.entrySet().iterator(); 
	        while(itr.hasNext()) { 
	             Entry<Integer, Integer> entry = itr.next(); 
	             if (f.isMax(entry.getKey()) && entry.getValue() == -1) {
	            	 f.addUnit(entry.getKey());
	             }
	        } 
		}
		
		if (inc == -1) {
			f.updateCounter(v, inc);
		}
	}
	
	private void change(int before, int after, PersistentFormula f) {
		Iterator<Entry<Integer, Integer>> itr = literal.entrySet().iterator(); 
        while(itr.hasNext()) { 
             Entry<Integer, Integer> entry = itr.next(); 
             if (entry.getValue() == -1) {
            	 if (before == -1) {
            		 f.updateCounter(entry.getKey(), -1);
            	 } else if (after == -1) {
            		 f.updateCounter(entry.getKey(), 1);
            	 }
             }
        } 
        
        if (before == -1) {
        	if (after == 1) {
        		f.incProved(1);
        	} else {
        		f.incDisproved(1);
        	}
        } else if (before == 0) {
        	if (after == -1) {
        		f.incDisproved(-1);
        	} else {
        		f.incDisproved(-1);
        		f.incProved(1);
        	}
        } else {
        	if (after == -1) {
        		f.incProved(-1);
        	} else {
        		f.incDisproved(1);
        		f.incProved(-1);
        	}
        }
	}
}
