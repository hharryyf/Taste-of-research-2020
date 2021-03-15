package qbfsolver;

public class BruteForce implements Solver {
	@Override
	public boolean solve(CnfExpression f) {
		// System.out.println("print formula:\n" + f);
		Result rr = ResultGenerator.getInstance();
		rr.setIteration(1 + rr.getIteration());
		if (f == null) return true;
		if (f.getn() <= 0) return true;
		int ret = f.evaluate();
		if (ret == 0) return false;
		if (ret == 1) return true;
		boolean type = f.peek().isMax();
		if (type) {
			Quantifier q = f.peekfreq(1, f.peek().isMax()).get(0);
			f.set(q.getVal());
			// System.out.println(f);
			f.dropquantifier(q.getVal());
			//System.out.println(f);
			f.simplify();
			f.commit();
			boolean res = solve(f);
			f.undo();
			if (res) {
				return true;
			}
			f.set(-q.getVal());
			f.dropquantifier(q.getVal());
			f.simplify();
			f.commit();
			res = solve(f);
			f.undo();
			return res;
		}
		
		
		Quantifier q = f.peekfreq(1, f.peek().isMax()).get(0);
		f.set(q.getVal());
		// System.out.println("set " + q.getVal());
		f.dropquantifier(q.getVal());
		f.simplify();
		f.commit();
		boolean res = solve(f);
		f.undo();
		if (!res) {
			return false;
		}
		f.set(-q.getVal());
		f.dropquantifier(q.getVal());
		f.simplify();
		f.commit();
		res = solve(f);
		f.undo();
		// System.out.println(f);
		return res;
	}
	
	public boolean solve_copy(CnfExpression f) {
		if (f == null) return true;
		if (f.getn() <= 0) return true;
		int ret = f.evaluate();
		if (ret == 0) return false;
		if (ret == 1) return true;
		CnfExpression f0 = f.duplicate();
		CnfExpression f1 = f.duplicate();
		Quantifier q = f.peekfreq(1, f.peek().isMax()).get(0);
		f1.set(q.getVal());
		f0.set(-q.getVal());
		f0.dropquantifier(q.getVal());
		f1.dropquantifier(q.getVal());
		f0.simplify();
		f1.simplify();
		if (q.isMax()) {
			return solve_copy(f0) || solve_copy(f1);
		}
		return solve_copy(f0) && solve_copy(f1);
	}
}

// unit propagation 

