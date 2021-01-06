package qbfsolver;

import java.util.ArrayList;
import java.util.List;

public class PNSNode {
	private boolean isMax = true;
	private List<PNSNode> child;
	private PNSNode parent;
	private int pn, dn;
	// number of variables 
	// that is going to be expanded
	private int varcount = 1;
	public PNSNode(CnfExpression f) {
		this.child = new ArrayList<PNSNode>();
		if (f.evaluate() == 1) {
			this.pn = 0;
			this.dn = 100000000;
		} else if (f.evaluate() == 0) {
			this.pn = 100000000;
			this.dn = 0;
		} else {
			this.pn = 1;
			this.dn = 1;
			this.isMax = f.peek().isMax();
			this.varcount = Math.min(f.maxSameQuantifier(this.isMax), ResultGenerator.getCommandLine().getBf());
			if (ResultGenerator.getCommandLine().getType() == 0 || 
				ResultGenerator.getCommandLine().getType() == 3) {
				if (this.isMax) {
					this.dn = (1 << varcount);
				} else {
					this.pn = (1 << varcount);
				}
			}
		}
		this.parent = null;
	}
	
	public boolean isMax() {
		return this.isMax;
	}
	
	public boolean isWin() {
		return this.dn >= 100000000;
	}
	
	public boolean isLost() {
		return this.pn >= 100000000;
	}
	
	public int getPn() {
		return this.pn;
	}
	
	public int getDn() {
		return this.dn;
	}
	
	public void setParent(PNSNode p) {
		this.parent = p;
	}
	
	public PNSNode getParent() {
		return this.parent;
	}
	
	public boolean isExpanded() {
		return !this.child.isEmpty();
	}
	
	public boolean isTerminal() {
		return this.isLost() || this.isWin();
	}
	
	public void expansion(CnfExpression f) {
		int i, j;
		List<Quantifier> list = f.peek(varcount, f.peek().isMax());
		for (i = 0 ; i < (1 << varcount); ++i) {
			CnfExpression fp = f.duplicate();
			for (j = 0 ; j < varcount; ++j) {
				if ((i & (1 << j)) == 0) {
					fp.set(list.get(j).getVal(), 0);
				} else {
					fp.set(list.get(j).getVal(), 1);
				}
				fp.dropquantifier();
			}
			
			fp.simplify();
			PNSNode nd = new PNSNode(fp);
			nd.setParent(this);
			this.child.add(nd);
		}
		/*
			CnfExpression f1 = f.duplicate();
			CnfExpression f2 = f.duplicate();
			f1.set(f.peek().getVal(), 0);
			f2.set(f.peek().getVal(), 1);
			f1.dropquantifier();
			f2.dropquantifier();
			// System.out.println("f1= " + f1);
			f1.simplify();
			f2.simplify();
			PNSNode n1 = new PNSNode(f1);
			PNSNode n2 = new PNSNode(f2);
			n1.setParent(this);
			n2.setParent(this);
			this.child.add(n1);
			this.child.add(n2);
		*/
	}
	
	public PNSNode MPN(CnfExpression f) {
		if (!this.isExpanded() && !this.isTerminal()) return null;
		PNSNode ret = null;
		int idx = -1, i;
		if (this.isMax()) {
			for (i = 0 ; i < child.size(); ++i) {
				if (ret == null || ret.getPn() > child.get(i).getPn()) {
					ret = child.get(i);
					idx = i;
				}
			}
		} else {
			for (i = 0 ; i < child.size(); ++i) {
				if (ret == null || ret.getDn() > child.get(i).getDn()) {
					ret = child.get(i);
					idx = i;
				}
			}
		}
		
		for (i = 0; i < varcount; ++i) {
			if ((idx & (1 << i)) == 0) {
				f.set(f.peek().getVal(), 0);
				f.dropquantifier();
			} else {
				f.set(f.peek().getVal(), 1);
				f.dropquantifier();
			}
		}
		f.simplify();
		return ret;
	}
	// check overflow INF
	public void backpropagation() {
		if (this.isTerminal() || !this.isExpanded()) return;
		if (this.isMax()) {
			this.pn = child.get(0).pn;
			this.dn = 0;
			for (PNSNode c : child) {
				this.pn = Math.min(this.pn, c.getPn());
				this.dn += c.getDn();
			}
		} else {
			this.pn = 0;
			this.dn = child.get(0).dn;
			for (PNSNode c : child) {
				this.pn += c.getPn();
				this.dn = Math.min(this.dn, c.getDn());
			}
		}
		if (this.isTerminal()) {
			this.child.clear();
			if (this.isWin()) {
				this.dn = 100000000;
			} else {
				this.pn = 100000000;
			}
		}
	}
}
// store a boolean win/lose/no, update the flag of the parent, no inf addition
