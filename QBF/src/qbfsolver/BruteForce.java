package qbfsolver;

public class BruteForce implements Solver {
	private int cnt = 0;
	@Override
	public boolean solve(CnfExpression f) {
		if (f == null) return true;
		if (f.getn() <= 0) return true;
		int ret = f.evaluate();
		if (ret == 0) return false;
		if (ret == 1) return true;
		CnfExpression f0 = f.duplicate();
		CnfExpression f1 = f.duplicate();
		Quantifier q = f.peek();
		f1.set(q.getVal());
		f0.set(-q.getVal());
		f0.dropquantifier();
		f1.dropquantifier();
		f0.simplify();
		f1.simplify();
		if (q.isMax()) {
			return solve(f0) || solve(f1);
		}
		return solve(f0) && solve(f1);
	}
	
	public boolean solve_depth(CnfExpression f, int d) {
		System.out.println(d + " " + cnt++);
		if (f == null) return true;
		if (f.getn() <= 0) return true;
		int ret = f.evaluate();
		if (ret == 0) return false;
		if (ret == 1) return true;
		CnfExpression f0 = f.duplicate();
		CnfExpression f1 = f.duplicate();
		Quantifier q = f.peek();
		f1.set(q.getVal());
		f0.set(-q.getVal());
		f0.dropquantifier();
		f1.dropquantifier();
		f0.simplify();
		f1.simplify();
		if (q.isMax()) {
			return solve_depth(f0, d + 1) || solve_depth(f1, d + 1);
		}
		return solve_depth(f0, d + 1) && solve_depth(f1, d + 1);
	}
}

// unit propagation 

