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
	public void set(int v, PersistentFormula f, int val, int id);
	public int evaluate();
	public Disjunction duplidate();
}
