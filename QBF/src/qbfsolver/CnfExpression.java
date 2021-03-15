package qbfsolver;

import java.util.List;

public interface CnfExpression {
	public int getn();
	public boolean hasQuantifier();
	public void addcnf(Disjunction c);
	public void addquantifier(Quantifier q);
	public Quantifier peek();
	public int maxSameQuantifier(boolean type);
	public List<Quantifier> peek(int count, boolean type);
	public void dropquantifier();
	public void dropquantifier(int v);
	/**
	 * Let v to be true
	 * @param v, positive means |v| is true, negative means |v| is false
	 */
	public void set(int v);
	public void setSatisfied(boolean val);
	public void normalize();
	public void simplify();
	public int evaluate();
	public CnfExpression duplicate();
	public void undo();
	public void commit();
	public int getFreq(int id);
	public int getNegfreq(int id);
	public int getPosfreq(int id);
	public List<Quantifier> peekfreq(int count, boolean type);
	public List<Quantifier> peekMom(int count, boolean type);
}
