package qbfsolver;

public interface CnfExpression {
	public int getn();
	public void addcnf(Disjunction c);
	public void addquantifier(Quantifier q);
	public Quantifier peek();
	public void dropquantifier();
	public void set(int v, int val);
	public void setSatisfied(boolean val);
	public void normalize();
	public int evaluate();
	public CnfExpression duplicate();
}
