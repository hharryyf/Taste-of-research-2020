package qbfsolver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Formula {
	private LinkedList<Cnf> cnf;
	private LinkedList<Quantifier> quantifier;
	private boolean satisfied;
	private int n;
	public Formula(int n) {
		this.cnf = new LinkedList<Cnf>();
		this.quantifier = new LinkedList<Quantifier>();
		this.satisfied = true;
		this.n = n;
	}
	
	public int getn() {
		return this.n;
	}
	
	public void addcnf(Cnf c) {
		this.cnf.add(c);
	}
	
	public void addquantifier(Quantifier q) {
		this.quantifier.add(q);
	}
	
	public Quantifier peek() {
		return this.quantifier.getFirst();
	}
	
	public void dropquantifier() {
		this.quantifier.removeFirst();
	}
	
	public void set(int v, int val) {
		for (Cnf c : this.cnf) {
			c.set(v, val);
		}
		
		 ListIterator<Cnf> iter = cnf.listIterator();
		 while (iter.hasNext()) {
			 Cnf c = iter.next();
			 int ret = c.evaluate();
			 if (ret == 1) {
				 iter.remove();
			 } else if (ret == 0) {
				 this.satisfied = false;
				 break;
			 }
		 }
	}
	
	// normalize the QBF such that it contains no free variables
	public void normalize() {
		Set<Integer> s = new HashSet<Integer>();
		for (Cnf c : this.cnf) {
			List<Integer> list = c.getSt();
			for (Integer it : list) {
				s.add(Math.abs(it));
			}
		}
		
		for (Quantifier q : quantifier) {
			s.remove(q.getVal());
		}
		
		Iterator<Integer> it = s.iterator();
	    while(it.hasNext()){
	        quantifier.addFirst(new Quantifier(true, it.next()));
	    }
	}
	
	int evaluate() {
		if (!this.satisfied) return 0;
		if (cnf.isEmpty()) return 1;
		return -1;
	}
	
	public Formula duplicate() {
		Formula f = new Formula(this.n);
		for (Quantifier q : this.quantifier) {
			f.addquantifier(q.duplicate());
		}
		
		for (Cnf c : this.cnf) {
			f.addcnf(c.duplidate());
		}
		
		f.satisfied = this.satisfied;
		return f;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.quantifier.toString() + " ");
		sb.append(this.cnf.toString());
		return sb.toString();
	}
}
