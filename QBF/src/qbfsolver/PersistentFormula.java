package qbfsolver;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

public class PersistentFormula implements CnfExpression {
	
	private int n, fcount;
	private List<Disjunction> formula;
	private List<List<Integer>> varToformula;
	private int quantifiercount = 0;
	private int unicount = 0;
	// private int existcount = 0;
	private int proved = 0, disproved = 0;
	// record the assigned variables in the last assignment
	private LinkedList<Set<Integer>> assigned;
	private int[] poscount;
	private int[] negcount;
	// (order,quantifier)
	private TreeSet<Pair<Integer, Quantifier>> quantifier;
	private TreeSet<Integer> pure, unit, useless;
	// a priority of the quantifier
	private int[] order;
	private boolean[] isexist;
	private Set<Integer> usedvar;
	private boolean normalized;
	public PersistentFormula(int n, int fcount) {
		int i;
		this.n = n;
		this.normalized = false;
		this.quantifiercount = n;
		this.fcount = fcount;
		this.formula = new ArrayList<>();
		this.assigned = new LinkedList<>();
		this.poscount = new int[n+1];
		this.negcount = new int[n+1];
		this.order = new int[n+1];
		this.isexist = new boolean[n+1];
		this.usedvar = new HashSet<>();
		this.quantifier = new TreeSet<>();
		this.varToformula = new ArrayList<>();
		for (i = 0 ; i <= n; ++i) {
			this.isexist[i] = true;
			this.poscount[i] = this.negcount[i] = this.order[i] = 0;
			this.varToformula.add(new ArrayList<>());
		}
		this.pure = new TreeSet<>();
		this.unit = new TreeSet<>();
		this.useless = new TreeSet<>();
	}
	
	public void incProved(int inc) {
		this.proved += inc;
	}
	
	public void incDisproved(int inc) {
		this.disproved += inc;
	}
	
	public boolean isMax(int val) {
		if (val < 0) val = -val;
		return isexist[val];
	}
	
	@Override
	public int getn() {
		return this.n;
	}

	@Override
	public boolean hasQuantifier() {
		return quantifiercount > 0;
	}

	@Override
	public void addcnf(Disjunction c) {
		List<Integer> ret = c.getLiteral();
		for (int i = 0 ; i < ret.size(); ++i) {
			int v = ret.get(i);
			
			this.updateCounter(v, 1);
			if (ret.size() == 1) this.addUnit(v);
			if (v < 0) v = -v;
			varToformula.get(v).add(formula.size());
		}
		formula.add(c);
		
	}
	
	public void setNormal() {
		this.normalized = true;
	}
	
	@Override
	public void addquantifier(Quantifier q) {
		this.quantifiercount++;
		// System.out.println(this.normalized);
		if (this.normalized) {
			// System.out.println("rec " + q);
			// System.err.println("enter here!");
			if (!this.quantifier.contains(new Pair<Integer, Quantifier>(order[q.getVal()], q))) {
				// System.out.println("insert " + q);
				if (!isMax(q.getVal())) {
					this.unicount++;
					// System.out.println("undo " + q.getVal());
				}
			}
			this.quantifier.add(new Pair<Integer, Quantifier>(order[q.getVal()], q));
		} else {
			if (this.quantifier.isEmpty()) {
				isexist[q.getVal()] = q.isMax();
				if (!q.isMax()) {
					order[q.getVal()] = 1;
				} else {
					order[q.getVal()] = 0;
				}
			} else {
				isexist[q.getVal()] = q.isMax();
				Pair<Integer, Quantifier> pre = this.quantifier.last();
				if (pre.getSecond().isMax() != q.isMax()) {
					order[q.getVal()] = pre.getFirst() + 1;
				} else {
					order[q.getVal()] = pre.getFirst();
				}
			}
			this.quantifier.add(new Pair<Integer, Quantifier>(order[q.getVal()], q));
		}
		
		
	}

	@Override
	public Quantifier peek() {
		if (this.normalized == false) {
			System.err.println("hasn't called normal!");
			System.exit(0);
		}
		
		return this.quantifier.first().getSecond();
	}

	@Override
	public int maxSameQuantifier(boolean type) {
		if (normalized == false) {
			System.err.println("hasn't called normal!");
			System.exit(0);
		}
		
		int count = 0;
		Iterator <Pair<Integer,Quantifier>> it = quantifier.iterator();
		while (count < 4 && it.hasNext()) {
			if (it.next().getSecond().isMax() != type) break;
			count++;
		}
		return count;
	}

	@Override
	public List<Quantifier> peek(int count, boolean type) {
		if (normalized == false) {
			System.err.println("hasn't called normal!");
			System.exit(0);
		}
		
		count = Math.max(Math.min(count, 4), 1);
		List<Quantifier> ret = new ArrayList<Quantifier>();
		int i = 0;
		Iterator <Pair<Integer, Quantifier>> it = quantifier.iterator();
		while (i < count && it.hasNext()) {
			Quantifier q = it.next().getSecond();
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
	public void dropquantifier() {
		// TODO Auto-generated method stub
		int v = this.quantifier.first().getSecond().getVal();
		this.dropquantifier(v);
	}

	@Override
	public void dropquantifier(int v) {
		if (v < 0) v = -v;
		if (this.quantifier.contains(new Pair<>(order[v], new Quantifier(isMax(v), v)))) {
			if (!isMax(v)) {
				this.unicount--; 
				//System.out.println("eliminate " + v);
			}
			this.quantifiercount--;
		}
		quantifier.remove(new Pair<>(order[v], new Quantifier(isMax(v), v)));
		this.usedvar.add(v);
	}

	@Override
	public void set(int v) {
		if (this.getFreq(v) == 0) return;
		for (Integer id : this.varToformula.get(Math.abs(v))) {
			Disjunction d = this.formula.get(id);
			d.set(Math.abs(v), this, v > 0 ? 1 : 0);
		}
		
		this.usedvar.add(v);
		// System.err.println("finish!");
	}
	
	private void unassign(int v) {
		if (v < 0) v = -v;
		// System.out.println("unset " + v);
		for (Integer id : this.varToformula.get(v)) {
			Disjunction d = this.formula.get(id);
			d.set(v, this, -1);
		}
		// System.out.println("finish! unset");
	}
	
	@Override
	public void setSatisfied(boolean val) {
		if (val) {
			this.proved = this.fcount;
			this.disproved = 0;
		} else {
			this.disproved = 1;
		}
	}

	@Override
	public void normalize() {
		
		for (int i = 1; i <= n; ++i) {
			if (quantifier.contains(new Pair<>(order[i], new Quantifier(isMax(i), i)))) continue;
			quantifier.add(new Pair<>(order[i], new Quantifier(isMax(i), i)));
			// this.addquantifier(q);
		}
		
		for (int i = 1; i <= n; ++i) {
			if (this.getFreq(i) == 0) {
				useless.add(i);
			}
		}
		
		eliminate_useless_quantifier();
		// System.out.println(quantifier);
		this.setNormal();
		this.unicount = 0;
		for (int i = 1; i <= n; ++i) {
			// System.out.println(i + " pos= " + poscount[i] + " neg= " + negcount[i]);
			if (this.getFreq(i) > 0 && !this.isMax(i)) this.unicount++;
		}
		// System.err.println(this.normalized);
	}

	@Override
	public void simplify() {
		boolean t1 = true;
		boolean t2 = false;
		while (t1 || t2) {
			t1 = unit_propagation();
			// t2 = pure_literal_elimination();
		}
		
		this.eliminate_useless_quantifier();
	}

	
	private boolean unit_propagation() {
		if (unit.isEmpty()) return false;
		while (!unit.isEmpty()) {
			int v = unit.pollFirst();
			// System.out.println("unit clause " + v);
			if (isMax(v)) {
				this.set(v);
			} else {
				this.set(-v);
			}
		}
		return true;
	}
	
	private boolean pure_literal_elimination() {
		if (pure.isEmpty()) return false;
		while (!pure.isEmpty()) {
			int v = pure.pollFirst();
			if (isMax(v)) {
				this.set(v);
			} else {
				this.set(-v);
			}
		}
		return true;
	}
	private void eliminate_useless_quantifier() {
		for (Integer id : this.useless) {
			this.dropquantifier(id);
		}
	}
	
	@Override
	public int evaluate() {
		// System.out.println(this.disproved + " " + this.proved + " " + this.fcount + " " + this.unicount);
		if (this.disproved > 0) return 0;
		if (this.proved == this.fcount) return 1;
		if (this.unicount == 0) {
			ISolver s = SolverFactory.newDefault ();
			s.setTimeout (900);
			s.newVar(this.n + 1);
			s.setExpectedNumberOfClauses(this.fcount);
			for (Disjunction d : this.formula) {
				if (d.evaluate() != -1) continue;
				List<Integer> list = d.getLiteral();
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
		}
		
		return -1;
	}

	@Override
	public CnfExpression duplicate() {
		return this;
	}

	@Override
	public int getFreq(int id) {
		if (id < 0) id = -id;
		return getNegfreq(id) + getPosfreq(id);
	}

	@Override
	public int getNegfreq(int id) {
		if (id < 0) id = -id;
		return negcount[id];
	}

	@Override
	public int getPosfreq(int id) {
		if (id < 0) id = -id;
		return poscount[id];
	}

	@Override
	public List<Quantifier> peekfreq(int count, boolean type) {
		ArrayList<Pair<Integer, Quantifier>> mp = new ArrayList<>();
		Iterator<Pair<Integer, Quantifier>> it = quantifier.iterator();
		while (it.hasNext()) {
			Quantifier q = it.next().getSecond();
			if (q.isMax() == type) {
				mp.add(new Pair<Integer, Quantifier>(this.getFreq(q.getVal()), q));
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
		
		return ret;
	}

	@Override
	public List<Quantifier> peekMom(int count, boolean type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void commit() {
		if (!this.usedvar.isEmpty()) {
			Set<Integer> st = new HashSet<>();
			for (Integer it : usedvar) {
				st.add(it);
			}
			usedvar.clear();
			this.assigned.add(st);
		}
	}
	
	@Override
	public void undo() {
		if (!this.assigned.isEmpty()) {
			Set<Integer> last = assigned.pollLast();
			for (Integer it : last) {
				it = Math.abs(it);
				this.unassign(it);
				// System.out.println("recover " + it);
				this.addquantifier(new Quantifier(isMax(it), it));
			}
		}
		this.unit.clear();
		this.pure.clear();
	}
	
	public void addUnit(int v) {
		this.unit.add(v);
	}
	
	public void updateCounter(int v, int inc) {
		
		if (v > 0) {
			poscount[v] += inc;
			if (negcount[v] > 0 && poscount[v] == 0) {
				pure.add(-v);
			}
			
			if (negcount[v] == 0 && poscount[v] > 0) {
				pure.add(v);
			}
			
			if (poscount[v] + negcount[v] == 0) {
				useless.add(v);
				pure.remove(v);
				pure.remove(-v);
				unit.remove(v);
				unit.remove(-v);
			}
			
			if (negcount[v] > 0 || poscount[v] > 0) {
				useless.remove(v);
			}
		} else {
			v = -v;
			negcount[v] += inc;
			if (negcount[v] > 0 && poscount[v] == 0) {
				pure.add(-v);
			}
			
			if (negcount[v] == 0 && poscount[v] > 0) {
				pure.add(v);
			}
			
			if (poscount[v] + negcount[v] == 0) {
				useless.add(v);
				pure.remove(-v);
				pure.remove(v);
				unit.remove(v);
				unit.remove(-v);
			}
			
			if (negcount[v] > 0 || poscount[v] > 0) {
				useless.remove(v);
			}
		}
		
		 
	}
	
	public String toString() {
		// System.out.println(poscount[9] + " " + negcount[9]);
		System.out.println(this.quantifier);
		System.out.println(this.assigned);
		for (int i = 1; i <= n; ++i) {
			System.out.println(i + ":[" + poscount[i] + "," + negcount[i] + "] ");
		}
		return  "[" + this.proved + ", " + this.disproved + ", " + this.unicount + ", " + this.evaluate() + "] " + this.formula.toString();
	}
}
