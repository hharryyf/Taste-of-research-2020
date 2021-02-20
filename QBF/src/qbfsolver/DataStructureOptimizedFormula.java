package qbfsolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
	// quantifier add time -> quantifier
	private TreeMap<Integer, Quantifier> quantifiers;
	// variable ->
	private TreeMap<Integer, Integer> varToquantifier;
	// integer arrays
	private TreeMap<Integer, Integer> varPos;
	private TreeMap<Integer, Integer> varNeg;
	// disjunction id -> disjunction
	private TreeMap<Integer, Disjunction> cnf;
	// variable id -> all disjunction id which this variable occurs
	private TreeMap<Integer, Integer> varBin;
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
		this.varBin = new TreeMap<>();
	}
	
	@Override
	public int getn() {
		return this.n;
	}
	
	private void dropvariable(int id) {
		if (id < 0) {
			System.err.println("bad! negative variable");
			System.exit(0);
		}
		int idx = this.varToquantifier.getOrDefault(id, -100000000);
		Quantifier q = this.quantifiers.getOrDefault(idx, null);
		if (q != null) {
			if (q.isMax()) {
				this.existcount--;
			} else {
				this.unicount--;
			}
		}
		this.varBin.remove(id);
		this.quantifiers.remove(idx);
		this.varToquantifier.remove(id);
		this.varPos.remove(id);
		this.varNeg.remove(id);
		this.varTocnf.remove(id);
	}
	
	@Override
	public void addcnf(Disjunction c) {
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
		} else if (list.size() == 2) {
			varBin.put(Math.abs(list.get(0)), this.getBinfreq(Math.abs(list.get(0))) + 1);
			varBin.put(Math.abs(list.get(1)), this.getBinfreq(Math.abs(list.get(1))) + 1);
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
	public boolean hasQuantifier() {
		return !quantifiers.isEmpty();
	}
	
	@Override
	public Quantifier peek() {
		return quantifiers.firstEntry().getValue();
	}

	@Override
	public void dropquantifier() {
		if (!quantifiers.isEmpty()) {
			/*Quantifier q = quantifiers.firstEntry().getValue();
			useless.remove(q.getVal());
			varToquantifier.remove(q.getVal());
			if (q.isMax()) {
				this.existcount--;
			} else {
				this.unicount--;
			}
			quantifiers.remove(quantifiers.firstKey());*/
			this.dropvariable(quantifiers.firstEntry().getValue().getVal());
		}
	}
	
	@Override
	public void dropquantifier(int v) {
		if (v < 0) v = -v;
		this.dropvariable(v);
	}
	
	@Override
	public int getFreq(int id) {
		return getNegfreq(id) + getPosfreq(id);
	}
	
	@Override
	public int getNegfreq(int id) {
		return varNeg.getOrDefault(id, 0);
	}
	
	@Override
	public int getPosfreq(int id) {
		return varPos.getOrDefault(id, 0);
	}
	
	public int getBinfreq(int id) {
		int ret = varBin.getOrDefault(id, 0);
		if (ret < 0) {
			System.err.println("frequency invalid!");
			System.exit(0);
		}
		return ret;
	}
	
	public void updatebinary(int v, int inc) {
		if (v < 0) v = -v;
		varBin.put(v, this.getBinfreq(v) + inc);
	}
	
	private int eval(int v) {
		// System.out.println("evaluate " + v + " " + getBinfreq(v));
		return getFreq(v);
	}
	
	@Override
	public List<Quantifier> peekMom(int count, boolean type) {
		List<Pair<Long, Quantifier>> list = new ArrayList<>();
		Iterator<Quantifier> it = quantifiers.values().iterator();
		HashMap<Integer, Integer> ng = new HashMap<>();
		HashMap<Integer, Integer> ps = new HashMap<>();
		while (it.hasNext()) {
			Quantifier q = it.next();
			if (q.isMax() != type) break;
			list.add(new Pair<Long, Quantifier>(0L, q));
		}
		
		int mx = 1000000000;
		for (int i = 0 ; i < list.size(); ++i) {
			if (varTocnf.getOrDefault(list.get(i).getSecond().getVal(), null) == null) continue;
			for(Integer id : varTocnf.get(list.get(i).getSecond().getVal())) {				
				if (cnf.containsKey(id)) {
					mx = Math.min(cnf.get(id).getSize(), mx);
				}
			}
		}
		
		for (int i = 0 ; i < list.size(); ++i) {
			int val = list.get(i).getSecond().getVal();
			if (varTocnf.getOrDefault(val, null) == null) continue;
			for(Integer id : varTocnf.get(list.get(i).getSecond().getVal())) {				
				if (cnf.containsKey(id) && cnf.get(id).getSize() == mx) {
					
					if (cnf.get(id).contains(val)) {
						ps.put(val, ps.getOrDefault(val, 0) + 1);
					}
					
					if (cnf.get(id).contains(-val)) {
						ng.put(val, ng.getOrDefault(val, 0) + 1);
					}
				}
			}
			
			int x = ps.getOrDefault(val, 0), nx = ng.getOrDefault(val, 0);
			list.get(i).setFirst(1L * (x + nx) * 1000000000 + 1L * x * nx);
		}
		
		Collections.sort(list);
		int j = list.size() - 1, i = 0;
		List<Quantifier> ret = new ArrayList<Quantifier>();
		while (i < count && j >= 0) {
			ret.add(list.get(j).getSecond());
			j--;
			i++;
		}
		return ret;
	}
	
	@Override
	public List<Quantifier> peekfreq(int count, boolean type) {
		ArrayList<Pair<Integer, Quantifier>> mp = new ArrayList<>();
		Iterator <Quantifier> it = quantifiers.values().iterator();
		while (it.hasNext()) {
			Quantifier q = it.next();
			if (q.isMax() == type) {
				mp.add(new Pair<Integer, Quantifier>(eval(q.getVal()), q));
			} else {
				break;
			}
		}
		
		Collections.sort(mp);
		count = Math.max(Math.min(count, 4), 1);
		List<Quantifier> ret = new ArrayList<Quantifier>();
		int i = 0, j = mp.size() - 1;
		while (i < count && j >= 0) {
			ret.add(mp.get(j).getSecond());
			i++;
			j--;
		}
		
		// System.out.println("freqbin " + mp);
		return ret;
	}
	
	@Override
	public void set(int w) {
		// pass in the literal that is satisfied w, recompute v based on abs
		int v = Math.abs(w);
		if (varTocnf.containsKey(v)) {
			for (Integer it : varTocnf.get(v)) {
				Disjunction d = cnf.get(it);
				if (d != null) {
					d.set(w, this, it);
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
				this.set(v);
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
					this.set(v);
				} else {
					this.set(-v);
				}
			} else if (hasNeg(v)) {
				if (q.isMax()) {
					this.set(-v);
				} else {
					this.set(v);
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
