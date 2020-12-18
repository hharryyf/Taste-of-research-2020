package qbfsolver;

public class BruteForce implements Solver {

	@Override
	public boolean solve(Formula f) {
		// System.out.println(f);
		if (f == null) return true;
		if (f.getn() <= 0) return true;
		int ret = f.evaluate();
		if (ret == 0) return false;
		if (ret == 1) return true;
		Formula f0 = f.duplicate();
		Formula f1 = f.duplicate();
		Quantifier q = f.peek();
		f1.set(q.getVal(), 1);
		f0.set(q.getVal(), 0);
		f0.dropquantifier();
		f1.dropquantifier();
		if (q.isMax()) {
			return solve(f0) || solve(f1);
		}
		
		return solve(f0) && solve(f1);
	}

}