package qbfsolver;

import java.util.List;

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
		boolean type = f.peek().isMax();
		if (type) {
			Quantifier q = f.peekfreq(1, f.peek().isMax()).get(0);
			f1.set(q.getVal());
			f0.set(-q.getVal());
			f0.dropquantifier(q.getVal());
			f1.dropquantifier(q.getVal());
			f0.simplify();
			f1.simplify();
			return solve(f0) || solve(f1);
		}
		int i, j;
		List<Quantifier> candidate = f.peekfreq(3, false);
		for (i = 0 ; i < (1 << candidate.size()); ++i) {
			CnfExpression fp = f.duplicate();
			for (j = 0 ; j < candidate.size(); ++j) {
				if ((i & (1 << j)) == 0) {
					fp.set(-candidate.get(j).getVal());
				} else {
					fp.set(candidate.get(j).getVal());
				}
				fp.dropquantifier(candidate.get(j).getVal());
			}
			
			fp.simplify();
			if (!solve(fp)) return false;
		}
		return true;
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

