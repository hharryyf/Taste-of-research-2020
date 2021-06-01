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
	private List<Set<Integer>> varToformula;
	private List<LinkedList<Integer>> usedformula;
	private int quantifiercount = 0;
	private int unicount = 0;
	// private int existcount = 0;
	private int proved = 0, disproved = 0;
	// record the assigned variables in the last assignment
	private LinkedList<LinkedList<Integer>> assigned;
	private int[] poscount;
	private int[] negcount;
	// (order,quantifier)
	private TreeSet<Pair<Integer, Quantifier>> quantifier;
	private HashSet<Integer> pure, unit, useless;
	// a priority of the quantifier
	private int[] order;
	private boolean[] isexist;
	private Set<Integer> provedformula;
	private LinkedList<Integer> usedvar;
	private boolean normalized;
	private LinkedList<Pair<Integer, Character>> currentassign;
	public PersistentFormula(int n, int fcount) {
		int i;
		this.n = n;
		this.normalized = false;
		this.quantifiercount = n;
		this.fcount = fcount;
		this.formula = new ArrayList<>();
		this.assigned = new LinkedList<>();
		this.provedformula = new TreeSet<>();
		this.poscount = new int[n+1];
		this.negcount = new int[n+1];
		this.order = new int[n+1];
		this.isexist = new boolean[n+1];
		this.usedvar = new LinkedList<>();
		this.quantifier = new TreeSet<>();
		this.varToformula = new ArrayList<>();
		this.usedformula = new ArrayList<>();
		for (i = 0 ; i <= n; ++i) {
			this.isexist[i] = true;
			this.poscount[i] = this.negcount[i] = this.order[i] = 0;
			this.varToformula.add(new HashSet<>());
			this.usedformula.add(new LinkedList<>());
		}
		this.pure = new HashSet<>();
		this.unit = new HashSet<>();
		this.useless = new HashSet<>();
		this.currentassign = new LinkedList<>();
	}
	
	public void incProved(int inc) {
		this.proved += inc;
	}
	
	public void incDisproved(int inc) {
		this.disproved += inc;
	}
	
	public int getLevel(int val) {
		if (val < 0) val = -val;
		return order[val];
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
	
	private boolean hasQuantifier(int v) {
		if (v < 0) v = -v;
		return this.quantifier.contains(new Pair<Integer, Quantifier>(order[v], new Quantifier(isMax(v), v)));
	}
	
	@Override
	public void addcnf(Disjunction c) {
		List<Integer> ret = c.getLiteral();
		for (int i = 0 ; i < ret.size(); ++i) {
			if (c.evaluate() == 1) {
				this.proved++;
				break;
			}
			int v = ret.get(i);
			this.updateCounter(v, 1);
			if (ret.size() == 1) this.addUnit(v);
			if (v < 0) v = -v;
			varToformula.get(v).add(formula.size());
			if (isMax(v) && c.getClass() == PersistentClause.class) {
				((PersistentClause) c).incExist(1);
			}
		}
		formula.add(c);
		// add an empty clause
		if (c.isEmpty()) {
			this.disproved++;
		} 
	}
	
	public void setNormal() {
		this.normalized = true;
	}
	
	@Override
	public void addquantifier(Quantifier q) {
		this.quantifiercount++;
		if (this.normalized) {
			if (!this.quantifier.contains(new Pair<Integer, Quantifier>(order[q.getVal()], q))) {
				if (!isMax(q.getVal())) {
					this.unicount++;
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
		if (this.hasQuantifier(v)) {
			if (!isMax(v)) {
				this.unicount--; 
			}
			this.quantifiercount--;
			quantifier.remove(new Pair<>(order[v], new Quantifier(isMax(v), v)));
			this.usedvar.add(v);
			this.currentassign.add(new Pair<Integer, Character>(v, 'N'));
		}
	}

	@Override
	public boolean set(int v) {
		if (this.evaluate() != -1) return false;
		if (!this.hasQuantifier(v)) return false;
		List<Integer> list = new ArrayList<Integer>(this.varToformula.get(Math.abs(v)));
		for (Integer id : list) {
			if (this.provedformula.contains(id)) continue;
			Disjunction d = this.formula.get(id);
			d.set(Math.abs(v), this, v > 0 ? 1 : 0, id);
			this.usedformula.get(Math.abs(v)).add(id);
		}
		
		//this.usedvar.add(Math.abs(v));
		this.dropquantifier(v);
		return true;
	}
	
	private boolean set(int v, char type) {
		if (this.set(v)) {
			this.currentassign.getLast().second = type;
		}
		return false;
	}
	
	private void unassign(int v) {
		if (v < 0) v = -v;
		while (!this.usedformula.get(v).isEmpty()) {
			int id = this.usedformula.get(v).pollLast();
			Disjunction d = this.formula.get(id);
			d.set(v, this, -1, id);
		}
		if (this.currentassign.isEmpty() || Math.abs(this.currentassign.getLast().first) != v) {
			System.err.println("there's a problem");
			System.err.println(currentassign);
			System.err.println(v);
			System.exit(0);
		} else {
			this.currentassign.removeLast();
		}
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
		}
		
		for (int i = 1; i <= n; ++i) {
			if (this.getFreq(i) == 0) {
				useless.add(i);
			}
		}
		
		eliminate_useless_quantifier();
		this.setNormal();
		this.unicount = 0;
		for (int i = 1; i <= n; ++i) {
			if (this.getFreq(i) > 0 && !this.isMax(i)) this.unicount++;
			if (this.getPosfreq(i) == 0 && this.getNegfreq(i) != 0) this.pure.add(-i);
			if (this.getPosfreq(i) > 0 && this.getNegfreq(i) == 0) this.pure.add(i);
		}
		
		for (int i = 0; i < formula.size(); ++i) {
			int lv = Integer.MAX_VALUE, cnt = 0, target = 0;
			List<Integer> ret = this.formula.get(i).getLiteral();
			for (Integer v : ret) {
				if (this.isMax(v)) {
					target = v;
					cnt++;
				} else {
					lv = Math.min(lv, target);
				}
			}
			
			if (target != 0 && cnt == 1 && lv > this.getLevel(target)) {
				this.addUnit(target);
			}
		}
		
		this.simplify();
		this.commit();
	}

	@Override
	public void simplify() {
		boolean t1 = true;
		boolean t2 = true;
		while (t1 || t2) {
			t1 = unit_propagation();
			t2 = pure_literal_elimination();
		}
		
		this.eliminate_useless_quantifier();
		this.unit.clear();
		this.pure.clear();
		this.useless.clear();
	}

	
	private boolean unit_propagation() {
		if (evaluate() != -1) return false; 
		if (unit.isEmpty()) return false;
		int v = unit.iterator().next();
		unit.remove(v);
		if (isMax(v)) {
			this.set(v, 'U');
		} else {
			this.set(-v, 'U');
		}
		return true;
	}
	
	private boolean pure_literal_elimination() {
		if (evaluate() != -1) return false;
		if (pure.isEmpty()) return false;
		int v = pure.iterator().next();
		pure.remove(v);
		if (isMax(v)) {
			this.set(v, 'P');
		} else {
			this.set(-v, 'P');
		}
		return true;
	}
	
	private void eliminate_useless_quantifier() {
		for (Integer id : this.useless) {
			this.dropquantifier(id);
			if (!this.currentassign.isEmpty() && Math.abs(this.currentassign.getLast().first) == Math.abs(id)) {
				this.currentassign.getLast().second = 'B';
			}
		}
	}
	
	@Override
	public int evaluate() {
		// System.out.println(this.disproved + " " + this.proved + " " + this.fcount + " " + this.unicount);
		if (this.disproved > 0) return 0;
		if (this.proved == this.fcount) return 1;
		if (this.unicount == 0) {
			ISolver s = SolverFactory.newDefault();
			s.setTimeout(900);
			s.newVar(this.n + 1);
			s.setExpectedNumberOfClauses(this.fcount);
			for (Disjunction d : this.formula) {
				if (d.evaluate() != -1) continue;
				List<Integer> list = d.getLiteral();
				int [] clause = list.stream().mapToInt(Integer::intValue).toArray();
				try {
					s.addClause(new VecInt(clause));
				} catch (ContradictionException e) {
					return 0;
				}
			}
			
			try {
				boolean curr = s.isSatisfiable();
				if (curr) {
					return 1;
				}
				
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
		// System.out.println(ret);
		return ret;
	}

	@Override
	public List<Quantifier> peekMom(int count, boolean type) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void commit() {
		if (!this.usedvar.isEmpty()) {
			LinkedList<Integer> st = new LinkedList<>(usedvar);
			usedvar.clear();
			this.assigned.add(st);
		}
		//System.out.println(assigned);
	}
	
	@Override
	public void undo() {
		if (!this.assigned.isEmpty()) {
			LinkedList<Integer> last = assigned.pollLast();
			while (!last.isEmpty()) {
				int it = last.pollLast();
				it = Math.abs(it);
				this.addquantifier(new Quantifier(isMax(it), it));
				this.unassign(it);
			}
		}
		this.pure.clear();
		this.unit.clear();
		this.useless.clear();
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
		
		if (poscount[v] > 0 && negcount[v] > 0) {
			pure.remove(v);
			pure.remove(-v);
		}
		 
	}
	
	public void addProved(int id, boolean direction) {
		if (direction) {
			this.provedformula.add(id);
			List<Integer> list = this.formula.get(id).getAll();
			for (Integer v : list) {
				this.varToformula.get(Math.abs(v)).remove(id);
			}
		} else {
			this.provedformula.remove(id);
			List<Integer> list = this.formula.get(id).getAll();
			for (Integer v : list) {
				this.varToformula.get(Math.abs(v)).add(id);
			}
		}
	}
	
	public LinkedList<Pair<Integer, Character>> getAssignment() {
		return this.currentassign;
	}
	
	public String toString() {
		System.out.println(this.quantifier);
		System.out.println(this.assigned);
		for (int i = 1; i <= n; ++i) {
			System.out.println(i + ":[" + poscount[i] + "," + negcount[i] + "] ");
		}
		return  "[" + this.proved + ", " + this.disproved + ", " + this.unicount + ", " + this.evaluate() + "] " + this.formula.toString();
	}
}
