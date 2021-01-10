package qbfsolver;

import java.util.ArrayList;
// import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class DataStructureOptimizedFormula implements CnfExpression {
	private int n;
	private TreeMap<Integer, Quantifier> quantifiers;
	private TreeMap<Integer, Integer> varToquantifier;
	private TreeMap<Integer, Integer> varPos;
	private TreeMap<Integer, Integer> varNeg;
	private TreeMap<Integer, Disjunction> cnf;
	private TreeMap<Integer, TreeSet<Integer>> varTocnf; 
	private TreeSet<Integer> unit;
	private TreeSet<Integer> useless;
	private TreeSet<Integer> pure;
	private boolean satisfied = true;
	private int unicount = 0;
	private int existcount = 0;
	public DataStructureOptimizedFormula(int n) {
		this.n = n;
		this.quantifiers = new TreeMap<>();
		this.varToquantifier = new TreeMap<>();
		this.varPos = new TreeMap<>();
		this.varNeg = new TreeMap<>();
		this.cnf = new TreeMap<>();
		this.varTocnf = new TreeMap<>();
		this.unit = new TreeSet<>();
		this.pure = new TreeSet<>();
		this.useless = new TreeSet<>();
		this.unicount = this.existcount = 0;
	}
	
	@Override
	public int getn() {
		return this.n;
	}
	
	private void dropvariable(int id) {
		assert(id > 0);
		int idx = this.varToquantifier.getOrDefault(id, -100000000);
		Quantifier q = this.quantifiers.getOrDefault(idx, null);
		if (q != null) {
			if (q.isMax()) {
				this.existcount--;
			} else {
				this.unicount--;
			}
		}
		this.quantifiers.remove(idx);
		this.varToquantifier.remove(id);
		this.varPos.remove(id);
		this.varNeg.remove(id);
		this.varTocnf.remove(id);
	}
	
	@Override
	public void addcnf(Disjunction c) {
		// TODO Auto-generated method stub
		int id = cnf.size() + 1;
		cnf.put(id, c);
		List<Integer> list = c.getVariable();
		for (Integer it : list) {
			if (!varTocnf.containsKey(Math.abs(it))) {
				varTocnf.put(Math.abs(it), new TreeSet<Integer>());
			}
			
			varTocnf.get(Math.abs(it)).add(id);
			updateCounter(it, 1);
		}
		
		if (list.size() == 1) {
			unit.add(list.get(0));
		}
	}
	
	public void removecnf(int id) {
		this.cnf.remove(id);
	}
	
	public boolean hasPos(int v) {
		return this.varPos.getOrDefault(Math.abs(v), 0) > 0;
	}
	
	public boolean hasNeg(int v) {
		return this.varNeg.getOrDefault(Math.abs(v), 0) > 0;
	}
	
	@Override
	public void addquantifier(Quantifier q) {
		int id = quantifiers.size() + 1;
		quantifiers.put(id, q);
		varToquantifier.put(q.getVal(), id);
		if (q.isMax()) {
			this.existcount++;
		} else {
			this.unicount++;
		}
	}

	@Override
	public Quantifier peek() {
		return quantifiers.firstEntry().getValue();
	}

	@Override
	public void dropquantifier() {
		if (!quantifiers.isEmpty()) {
			Quantifier q = quantifiers.firstEntry().getValue();
			useless.remove(q.getVal());
			varToquantifier.remove(q.getVal());
			if (q.isMax()) {
				this.existcount--;
			} else {
				this.unicount--;
			}
			quantifiers.remove(quantifiers.firstKey());
		}
	}

	@Override
	public void set(int v, int val) {
		if (v < 0) v = -v;
		if (varTocnf.containsKey(v)) {
			for (Integer it : varTocnf.get(v)) {
				Disjunction d = cnf.get(it);
				if (d != null) {
					d.set(v, val, this, it);
				}
			}
		}
		varTocnf.remove(v);
	}
	
	
	public void addUnitVariable(int v) {
		unit.add(v);
	}
	
	public void deleteUnitVariable(int v) {
		unit.remove(v);
	}
	
	public void updateCounter(int v, int inc) {
		
		if (v < 0) {
			varNeg.put(-v, varNeg.getOrDefault(-v, 0) + inc);
		} else {
			varPos.put(v, varPos.getOrDefault(v, 0) + inc);
		}
		
		boolean haspos = hasPos(v), hasneg = hasNeg(v);
		if ((haspos && !hasneg) || (!haspos && hasneg)) {
			this.pure.add(v);
		} else {
			this.pure.remove(v);
			this.pure.remove(-v);
		}
		
		if (!haspos && !hasneg) {
			useless.add(v);
		} else {
			useless.remove(v);
		}
	}
	
	@Override
	public void setSatisfied(boolean val) {
		this.satisfied = val;
	}
	
	@Override
	public void normalize() {
		Set<Integer> s = new HashSet<Integer>();
		for (Disjunction c : this.cnf.values()) {
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
	    
	    for (Integer q : this.varToquantifier.keySet()) {
	    	if (!hasPos(q) && !hasNeg(q)) useless.add(q);
	    }
	    eliminate_useless_quantifiers();
	    this.existcount = this.unicount = 0;
	    for (Quantifier q : this.quantifiers.values()) {
	    	if (q.isMax()) {
	    		this.existcount++;
	    	} else {
	    		this.unicount++;
	    	}
	    }
	}
	
	@Override
	public void simplify() {
		boolean t1 = true, t2 = true;
		while (t1 || t2) {
			t1 = this.unit_propagation();
			t2 = this.pure_literal_elimination();
		}
		
		this.eliminate_useless_quantifiers();
	}
	
	private void eliminate_useless_quantifiers() {
		Iterator<Integer> it = useless.iterator();
		while (it.hasNext()) {
			dropvariable(Math.abs(it.next()));
		}
		useless.clear();
	}
	
	private boolean unit_propagation() {
		if (this.terminal() != -1) return false;
		if (unit.isEmpty()) return false;
		int v = unit.pollFirst();
		if (varToquantifier.containsKey(Math.abs(v))) {
			if (quantifiers.get(varToquantifier.get(Math.abs(v))).isMax()) {
				if (v < 0) {
					this.set(-v, 0);
				} else {
					this.set(v, 1);
				}
			} else {
				this.setSatisfied(false);
			}
		}
		return true;
	}
	
	private boolean pure_literal_elimination() {
		if (pure.isEmpty() || this.terminal() != -1) return false;
		while (!pure.isEmpty()) {
			int v = pure.pollFirst();
			pure.remove(v);
			if (v < 0) v = -v;
			if (hasPos(v) && hasNeg(v)) {
				System.out.println("we found one " + v);
				System.exit(0);
			}
			if (!varToquantifier.containsKey(v)) continue;
			Quantifier q = this.quantifiers.get(this.varToquantifier.get(v));
			if (q == null) continue;
			if (hasPos(v)) {
				if (q.isMax()) {
					this.set(v, 1);
				} else {
					this.set(v, 0);
				}
			} else if (hasNeg(v)) {
				if (q.isMax()) {
					this.set(v, 0);
				} else {
					this.set(v, 1);
				}
			}
		}
		
		pure.clear();
		return true;
	}
	
	public int terminal() {
		if (!this.satisfied) return 0;
		if (this.quantifiers.isEmpty()) return 1;
		return -1;
	}
	
	@Override
	public int evaluate() {
		int ret = this.terminal();
		if (ret != -1) return ret;
		if (this.unicount == 0) {
			// System.out.println("last quantifier block! number of useful quantifiers left " + this.existcount);
			ISolver s = SolverFactory.newDefault ();
			s.setTimeout (900);
			s.newVar(this.n + 1);
			s.setExpectedNumberOfClauses(this.cnf.size());
			for (Disjunction d : this.cnf.values()) {
				List<Integer> list = d.getVariable();
				int [] clause = list.stream().mapToInt(Integer::intValue).toArray();
				try {
					s.addClause(new VecInt(clause));
				} catch (ContradictionException e) {
					e.printStackTrace();
				}
			}
			
			try {
				boolean curr = s.isSatisfiable();
				if (curr) return 1;
				return 0;
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
			return -1;
			
		} else if (this.existcount == 0) {
			return 0;
		}
		return -1;
	}

	@Override
	public DataStructureOptimizedFormula duplicate() {
		DataStructureOptimizedFormula ret = new DataStructureOptimizedFormula(this.n); 
		for (Quantifier q : this.quantifiers.values()) {
			ret.addquantifier(q.duplicate());
		}
		
		for (Disjunction d : this.cnf.values()) {
			ret.addcnf(d.duplidate());
		}
		
		ret.setSatisfied(this.satisfied);
		return ret;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("evaluation= " + terminal());
		sb.append(this.quantifiers.values().toString() + " ");
		sb.append(this.cnf.toString());
		sb.append(this.varTocnf.toString());
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
