package qbfsolver;

import java.util.ArrayList;
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
	public boolean hasQuantifier() {
		return !this.quantifiers.isEmpty();
	}
	
	@Override
	public void dropquantifier() {
		if (!quantifiers.isEmpty()) {
			varToquantifier.remove(quantifiers.firstEntry().getValue().getVal());
			quantifiers.remove(quantifiers.firstKey());
		}
	}

	@Override
	public void set(int v) {
		for (Disjunction d : cnf) {
			if (v > 0) {
				d.set(v, 1);
			} else {
				d.set(-v, 0);
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

	@Override
	public void normalize() {
		Set<Integer> s = new HashSet<Integer>();
		for (Disjunction c : this.cnf) {
			List<Integer> list = c.getLiteral();
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
		boolean t1 = true, t2 = true;
		//System.out.println("in");
		while (t1 || t2) {
			t1 = unit_propagation();
			t2 = pure_literal_elimination();
		}
		eliminate_useless_quantifiers();
		//System.out.println("out");
	}
	
	private void eliminate_useless_quantifiers() {
		Set<Integer> s = new HashSet<Integer>();
		
		for (Quantifier q : this.quantifiers.values()) {
			s.add(q.getVal());
		}
		
		for (Disjunction c : this.cnf) {
			List<Integer> list = c.getLiteral();
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
	
	private boolean unit_propagation() {
		boolean find = true;
		boolean success = false;
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
					
					this.set(var);
					find = true;
					success = true;
					break;
				}
			}
			
		}
		
		return success;
	}
	
	private boolean pure_literal_elimination() {
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
				if (entry.getValue() == 1) {
					this.set(entry.getKey());
				} else {
					this.set(-entry.getKey());
				}
			}
			
			return !mp.entrySet().isEmpty();
		}
		
		return false;
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
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(this.quantifiers.values().toString() + " ");
		sb.append(this.cnf.toString());
		return sb.toString();
	}

	@Override
	public List<Quantifier> peek(int count, boolean type) {
		count = Math.max(Math.min(count, 4), 1);
		List<Quantifier> ret = new ArrayList<Quantifier>();
		int i = 0;
		Iterator <Quantifier> it = quantifiers.values().iterator();
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
	
	@Override
	public int maxSameQuantifier(boolean type) {
		int count = 0;
		Iterator <Quantifier> it = quantifiers.values().iterator();
		while (count < 4 && it.hasNext()) {
			if (it.next().isMax() != type) break;
			count++;
		}
		return count;
	}
}
