package qbfsolver;

import java.util.List;

public interface CnfExpression {
	public int getn();
	public void addcnf(Disjunction c);
	public void addquantifier(Quantifier q);
	public Quantifier peek();
	public int maxSameQuantifier(boolean type);
	public List<Quantifier> peek(int count, boolean type);
	public void dropquantifier();
	public void set(int v, int val);
	public void setSatisfied(boolean val);
	public void normalize();
	public void simplify();
	public int evaluate();
	public CnfExpression duplicate();
}
