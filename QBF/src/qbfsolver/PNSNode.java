package qbfsolver;

import java.util.ArrayList;
import java.util.List;

public class PNSNode implements TreeNode {
	private Formula f;
	private List<PNSNode> child;
	private PNSNode parent;
	private int pn, dn;
	public PNSNode(Formula f) {
		this.f = f;
		this.child = new ArrayList<PNSNode>();
		if (f.evaluate() == 1) {
			this.pn = 0;
			this.dn = 10000000;
		} else if (f.evaluate() == 0) {
			this.pn = 10000000;
			this.dn = 0;
		} else {
			this.pn = 1;
			this.dn = 1;
		}
		this.parent = null;
	}
	
	@Override
	public boolean isWin() {
		return this.dn >= 10000000;
	}
	
	@Override
	public boolean isLost() {
		return this.pn >= 10000000;
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
	
	public TreeNode getParent() {
		return this.parent;
	}
	
	public boolean isExpanded() {
		return !this.child.isEmpty();
	}
	
	public boolean isTerminal() {
		return this.f.evaluate() != -1;
	}
	
	@Override
	public void expansion() {
		Formula f1 = f.duplicate();
		Formula f2 = f.duplicate();
		f1.set(f.peek().getVal(), 0);
		f2.set(f.peek().getVal(), 1);
		f1.dropquantifier();
		f2.dropquantifier();
		PNSNode n1 = new PNSNode(f1);
		PNSNode n2 = new PNSNode(f2);
		n1.setParent(this);
		n2.setParent(this);
		this.child.add(n1);
		this.child.add(n2);
	}
	
	@Override
	public TreeNode MPN() {
		if (!this.isExpanded() && !this.isTerminal()) return null;
		PNSNode ret = null;
		if (this.f.peek().isMax()) {
			for (PNSNode nd : this.child) {
				if (ret == null || ret.getPn() > nd.getPn()) {
					ret = nd;
				}
			}
		} else {
			for (PNSNode nd : this.child) {
				if (ret == null || ret.getDn() > nd.getDn()) {
					ret = nd;
				}
			}
		}
		return ret;
	}
	
	@Override
	public void backpropagation() {
		if (this.isTerminal() || !this.isExpanded()) return;
		if (this.f.peek().isMax()) {
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
	}
}
