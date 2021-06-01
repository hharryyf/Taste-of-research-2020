package qbfsolver;

import java.util.ArrayList;
import java.util.List;

public class DeepPnNode {
	private boolean isMax = true;
	private int pn, dn;
	private double deep;
	private int depth;
	private DeepPnNode parent;
	private List<DeepPnNode> child;
	private List<Quantifier> candidate;
	private int varcount = 1;
	public static int inf = 120000000;
	public DeepPnNode(CnfExpression f, int depth) {
		Result res = ResultGenerator.getInstance();
		res.setNode();
		this.deep = 1.0 / depth;
		this.depth = depth;
		this.child = new ArrayList<>();
		if (f.evaluate() == 1) {
			this.pn = 0;
			this.dn = inf;
		} else if (f.evaluate() == 0) {
			this.pn = inf;
			this.dn = 0;
		} else {
			/*if (this.depth > 30) {
				Solver s = new BruteForce();
				boolean ret = s.solve(f);
				if (ret == true) {
					this.pn = 0;
					this.dn = inf;
				} else {
					this.pn = inf;
					this.dn = 0;
				}
			} else {*/
				this.pn = 1;
				this.dn = 1;
				this.isMax = f.peek().isMax();
				if (this.isMax) {
					this.varcount = Math.min(f.maxSameQuantifier(this.isMax), ResultGenerator.getCommandLine().getBfE());
				} else {
					this.varcount = Math.min(f.maxSameQuantifier(this.isMax), ResultGenerator.getCommandLine().getBfU());
				}
				if (ResultGenerator.getCommandLine().getType() == 0 || 
					ResultGenerator.getCommandLine().getType() == 3) {
					if (this.isMax) {
						this.dn = (1 << varcount);
					} else {
						this.pn = (1 << varcount);
					}
				}
			//}
		}
		
		this.parent = null;
	}
	
	public boolean isTerminal() {
		return this.child.isEmpty();
	}
	
	
	
	public boolean isSolved() {
		return this.pn >= inf || this.dn >= inf;
	}
	
	public boolean isLost() {
		return this.pn >= inf;
	}
	
	public boolean isWin() {
		return this.dn >= inf;
	}
	
	public int getPn() {
		return this.pn;
	}
	
	public int getDn() {
		return this.dn;
	}
	
	public int getDelta() {
		if (this.isMax) return this.dn;
		return this.pn;
	}
	
	public int getPhi() {
		if (this.isMax) return this.pn;
		return this.dn;
	}
	
	public double dpn() {
		double R = ResultGenerator.getCommandLine().getR();
		return (1.0 - 1.0 / this.getDelta()) * R + this.deep * (1.0 - R);
	}
	
	public void expansion(CnfExpression f) {
		if (f.evaluate() != -1) {
			System.err.println("bad!, invalid expansion");
			System.exit(0);
		}
		
		int i, j;
		//System.out.println("expansion!");
		this.candidate = f.peek(varcount, f.peek().isMax());		
		for (i = 0 ; i < (1 << varcount); ++i) {
			CnfExpression fp = f.duplicate();
			for (j = 0 ; j < varcount; ++j) {
				if ((i & (1 << j)) == 0) {
					fp.set(-candidate.get(j).getVal());
				} else {
					fp.set(candidate.get(j).getVal());
				}
				if (f.getClass() != PersistentFormula.class) {
					fp.dropquantifier(candidate.get(j).getVal());
				}
			}
			
			fp.simplify();
			f.commit();
			DeepPnNode nd = new DeepPnNode(fp, this.depth + 1);
			nd.parent = this;
			this.child.add(nd);
			f.undo();
		}
		
	}
	
	public DeepPnNode MPN(CnfExpression f) {
		if (this.isTerminal()) return null;
		DeepPnNode ret = null;
		int idx = -1, i;
		for (i = 0 ; i < child.size(); ++i) {
			if ((ret == null && !child.get(i).isSolved()) 
			   || (ret != null && ret.dpn() >= child.get(i).dpn() && !child.get(i).isSolved())) {
				ret = child.get(i);
				idx = i;
			}
		} 
		
		if (idx == -1) {
			System.err.println("No such node");
			System.exit(0);
		}
		// System.out.println("MPN= " + idx);
		for (i = 0; i < varcount; ++i) {
			if ((idx & (1 << i)) == 0) {
				f.set(-candidate.get(i).getVal());
			} else {
				f.set(candidate.get(i).getVal());	
			}
			//f.dropquantifier(f.peek().getVal());
			// f.dropquantifier();
			if (f.getClass() != PersistentFormula.class) {
				f.dropquantifier(candidate.get(i).getVal());
			}
		}
		f.simplify();
		f.commit();
		return ret;
	}
	
	public void backpropagation() {
		// System.out.println("backprop");
		if (this.isTerminal() || this.isSolved()) return;
		DeepPnNode curr = null;
		boolean meet = false;
		if (this.isMax()) {
			this.pn = child.get(0).pn;
			this.dn = 0;
			for (DeepPnNode c : child) {
				this.pn = Math.min(this.pn, c.getPn());
				this.dn += c.getDn();
				meet = (c.getDn() >= inf) || meet;
				if ((curr == null && !c.isSolved()) || (curr != null && !c.isSolved() && c.dpn() <= curr.dpn()))  {
					curr = c;
				}
			}
		} else {
			this.pn = 0;
			this.dn = child.get(0).dn;
			for (DeepPnNode c : child) {
				this.pn += c.getPn();
				meet = (c.getPn() >= inf) || meet;
				this.dn = Math.min(this.dn, c.getDn());
				if ((curr == null && !c.isSolved()) || (curr != null && !c.isSolved() && c.dpn() <= curr.dpn())) {
					curr = c;
				}
			}
		}
		
		if (curr != null) {
			this.deep = curr.deep;
		}
		
		if (this.isSolved()) {
			this.child.clear();
			if (!meet && this.pn > inf) {
				System.err.println("inf too small!");
				System.exit(0);
			}
			
			if (!meet && this.dn > inf) {
				System.err.println("inf too small!");
				System.exit(0);
			}
			this.pn = Math.min(this.pn, inf);
			this.dn = Math.min(this.dn, inf);
		}		
	}
	
	public DeepPnNode getParent() {
		return this.parent;
	}
	
	public boolean isMax() {
		return this.isMax;
	}
}
