package qbfsolver;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

public class Formula implements CnfExpression {
	private LinkedList<Disjunction> cnf;
	private LinkedList<Quantifier> quantifier;
	private boolean satisfied;
	private int n;
	public Formula(int n) {
		this.cnf = new LinkedList<Disjunction>();
		this.quantifier = new LinkedList<Quantifier>();
		this.satisfied = true;
		this.n = n;
	}
	
	@Override
	public int getn() {
		return this.n;
	}
	
	@Override
	public void addcnf(Disjunction c) {
		this.cnf.add(c);
	}
	
	@Override
	public void addquantifier(Quantifier q) {
		this.quantifier.add(q);
	}
	
	@Override
	public Quantifier peek() {
		return this.quantifier.getFirst();
	}
	
	@Override
	public boolean hasQuantifier() {
		return !this.quantifier.isEmpty();
	}
	
	@Override
	public void dropquantifier() {
		if (!this.quantifier.isEmpty()) {
			this.quantifier.removeFirst();
		}
	}
	
	@Override
	public void simplify() {
		return;
	}
	
	@Override
	public void set(int v) {
		for (Disjunction c : this.cnf) {
			if (v > 0) {
				c.set(v, 1);
			} else {
				c.set(-v, 0);
			}
		}
		
		 ListIterator<Disjunction> iter = cnf.listIterator();
		 while (iter.hasNext()) {
			 Disjunction c = iter.next();
			 int ret = c.evaluate();
			 if (ret == 1) {
				 iter.remove();
			 } else if (ret == 0) {
				 this.satisfied = false;
				 break;
			 }
		 }
	}
	
	@Override
	public void setSatisfied(boolean val) {
		this.satisfied = val;
	}
	
	// normalize the QBF such that it contains no free variables
	@Override
	public void normalize() {
		Set<Integer> s = new HashSet<Integer>();
		for (Disjunction c : this.cnf) {
			List<Integer> list = c.getLiteral();
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
	    // make sure existential quantifier always go first
	    if (!this.quantifier.isEmpty()) {
	    	if (!this.quantifier.getFirst().isMax()) {
	    		this.quantifier.addFirst(new Quantifier(true, this.n + 1));
	    	}
	    }
	}
	
	@Override
	public int evaluate() {
		if (!this.satisfied) return 0;
		if (cnf.isEmpty()) return 1;
		return -1;
	}
	
	@Override
	public Formula duplicate() {
		Formula f = new Formula(this.n);
		for (Quantifier q : this.quantifier) {
			f.addquantifier(q.duplicate());
		}
		
		for (Disjunction c : this.cnf) {
			f.addcnf(c.duplidate());
		}
		
		f.setSatisfied(this.satisfied);
		return f;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.quantifier.toString() + " ");
		sb.append(this.cnf.toString());
		return sb.toString();
	}

	@Override
	public List<Quantifier> peek(int count, boolean type) {
		count = Math.max(Math.min(count, 4), 1);
		List<Quantifier> ret = new ArrayList<Quantifier>();
		int i = 0;
		Iterator <Quantifier> it = quantifier.iterator();
		while (i < count && it.hasNext()) {
			Quantifier q = it.next();
			if (q.isMax() == type) {
				ret.add(q);
			} else {
				break;
			}
			i++;
		}
		return ret;
	}
	
	public int maxSameQuantifier(boolean type) {
		Iterator <Quantifier> it = quantifier.iterator();
		int count = 0;
		while (count < 4 && it.hasNext()) {
			if (it.next().isMax() != type) break;
			count++;
		}
		return count;
	}
}
