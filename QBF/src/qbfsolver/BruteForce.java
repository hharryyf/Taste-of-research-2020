package qbfsolver;

public class BruteForce implements Solver {

	@Override
	public boolean solve(CnfExpression f) {
		// System.out.println(f);
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

}

// unit propagation 

