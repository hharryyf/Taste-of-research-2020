package qbfsolver;

import java.util.List;

public interface Disjunction {
	public boolean hasvar(int val);
	public boolean contains(int val);
	public boolean isEmpty();
	public List<Integer> getLiteral();
	public List<Integer> getVariable();
	public int getSize();
	public void add(int val);
	public void set(int v, int val);
	public void set(int w, DataStructureOptimizedFormula f, int id);
	/**
	 * set |v| to be -1, default do nothing
	 * @param v
	 */
	public void undo(int v);
	public int evaluate();
	public Disjunction duplidate();
}
