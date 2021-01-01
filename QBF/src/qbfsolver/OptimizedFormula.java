package qbfsolver;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class OptimizedFormula implements CnfExpression {
	// quantifier_id -> Quantifier
	private TreeMap<Integer, Quantifier> quantifiers;
	private TreeMap<Integer, Integer> varToquantifier;
	private LinkedList<Disjunction> cnf;
	private boolean satisfied = true;
	private int n;
	public OptimizedFormula(int n) {
		this.quantifiers = new TreeMap<>();
		this.varToquantifier = new TreeMap<>();
		this.cnf = new LinkedList<>();
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
		int id = quantifiers.size() + 1;
		quantifiers.put(id, q);
		varToquantifier.put(q.getVal(), id);
	}

	@Override
	public Quantifier peek() {
		
		return quantifiers.firstEntry().getValue();
	}

	@Override
	public void dropquantifier() {
		if (!quantifiers.isEmpty()) {
			varToquantifier.remove(quantifiers.firstEntry().getValue().getVal());
			quantifiers.remove(quantifiers.firstKey());
		}
	}

	@Override
	public void set(int v, int val) {
		for (Disjunction d : cnf) {
			d.set(v, val);
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

	@Override
	public void normalize() {
		Set<Integer> s = new HashSet<Integer>();
		for (Disjunction c : this.cnf) {
			List<Integer> list = c.getSt();
			for (Integer it : list) {
				s.add(Math.abs(it));
			}
		}
		
		for (Quantifier q : this.quantifiers.values()) {
			s.remove(q.getVal());
		}
		
		int idx = 0;
		Iterator<Integer> it = s.iterator();
	    while(it.hasNext()){
	    	int val = it.next();
	        this.quantifiers.put(idx, new Quantifier(true, val));
	        this.varToquantifier.put(val, idx);
	        idx--;
	    }
	    
	    eliminate_useless_quantifiers();
	}
	
	@Override
	public void simplify() {
		unit_propagation();
		eliminate_useless_quantifiers();
		pure_literal_elimination();
		eliminate_useless_quantifiers();
	}
	
	private void eliminate_useless_quantifiers() {
		Set<Integer> s = new HashSet<Integer>();
		
		for (Quantifier q : this.quantifiers.values()) {
			s.add(q.getVal());
		}
		
		for (Disjunction c : this.cnf) {
			List<Integer> list = c.getSt();
			for (Integer it : list) {
				s.remove(Math.abs(it));
			}
		}
		
		Iterator<Integer> it = s.iterator();
	    while(it.hasNext()){
	    	int val = it.next();
	    	int id = varToquantifier.get(val);
	    	varToquantifier.remove(val);
	    	quantifiers.remove(id);
	    }
	}
	
	private void unit_propagation() {
		boolean find = true;
		// System.out.println("enter");
		while (find) {
			find = false;
			for (Disjunction d : cnf) {
				if (d.evaluate() == -1 && d.getSize() == 1) {
					int var = d.getVariable().get(0);
					// System.out.println(var);
					int id = varToquantifier.get(Math.abs(var));
					boolean isMax = quantifiers.get(id).isMax();
					if (!isMax) {
						this.satisfied = false;
						break;
					}
					
					if (var < 0) {
						this.set(-var, 0);
					} else {
						this.set(var, 1);
					}
					find = true;
					// System.out.println("find!");
					break;
				}
			}
			
		}
		// System.out.println("exit");
	}
	
	private void pure_literal_elimination() {
		if (evaluate() == -1) {
			TreeMap<Integer, Integer> mp = new TreeMap<>();
			for (Quantifier q : this.quantifiers.values()) {
				boolean haspositive = false, hasnegative = false;
				for (Disjunction d : this.cnf) {
					if (d.evaluate() == -1 && d.contains(q.getVal())) {
						haspositive = true;
					}
					
					if (d.evaluate() == -1 && d.contains(-q.getVal())) {
						hasnegative = true;
					}
				}
				
				if (haspositive && !hasnegative) {
					if (q.isMax()) {
						mp.put(q.getVal(), 1);
					} else {
						mp.put(q.getVal(), 0);
					}
				}
				
				if (!haspositive && hasnegative) {
					if (q.isMax()) {
						mp.put(q.getVal(), 0);
					} else {
						mp.put(q.getVal(), 1);
					}
				}
			}
			
			for (Map.Entry<Integer, Integer> entry : mp.entrySet()) {
				this.set(entry.getKey(), entry.getValue());
			}
		}
	}
	
	@Override
	public int evaluate() {
		if (!this.satisfied) return 0;
		if (this.quantifiers.isEmpty()) return 1;
		return -1;
	}

	@Override
	public OptimizedFormula duplicate() {
		OptimizedFormula ret = new OptimizedFormula(this.n); 
		for (Quantifier q : this.quantifiers.values()) {
			ret.addquantifier(q.duplicate());
		}
		
		for (Disjunction d : this.cnf) {
			ret.cnf.add(d.duplidate());
		}
		
		ret.setSatisfied(this.satisfied);
		//System.out.println("before copy " + this);
		//System.out.println("after copy " + ret);
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.quantifiers.values().toString() + " ");
		sb.append(this.cnf.toString());
		return sb.toString();
	}

}
