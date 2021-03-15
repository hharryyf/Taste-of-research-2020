package qbfsolver;

import java.util.Stack;

public class PNSv2 implements Solver {
	private int maxT = 1000000;
	public PNSv2() {
		
	}
	
	public PNSv2(int maxT) {
		this.maxT = maxT;
	}
	
	@Override
	public boolean solve(CnfExpression f) {
		// System.out.println(f);
		PNSNode root = new PNSNode(f), curr = root;
		int i = 0, tolvisited = 0;
		// int mxd = 0;
		Stack<CnfExpression> stk = new Stack<CnfExpression>();
		while (i <= this.maxT && !root.isWin() && !root.isLost()) {
			if (i % 1000 == 0) {
				System.out.println("Iteration #" + i + " pn = " + root.getPn() + " dn= " + root.getDn());
			}
			
			if (stk.empty()) {
				stk.push(f.duplicate());
			}
			
			CnfExpression fp = stk.peek().duplicate();
			if (curr == null) curr = root;
			PNSNode it;
			while (true) {
				// System.out.println(fp);
				it = curr.MPN(fp);
				if (it == null) break;
				stk.push(fp.duplicate());
				curr = it;
				tolvisited++;
			}
			curr.expansion(fp);
			//mxd = Math.max(mxd, stk.size() + 1);
			while (curr != null) {
				int pn = curr.getPn(), dn = curr.getDn();
				curr.backpropagation();
				if (pn == curr.getPn() && dn == curr.getDn()) break;
				curr = curr.getParent();
				if (!stk.isEmpty()) {
					stk.peek().undo();
				}
				stk.pop();
				tolvisited++;
			}
			
			i++;
		}
		
		Result res = ResultGenerator.getInstance();
		System.out.println("total visited nodes= " + tolvisited);
		System.out.println("Iteration " + i + " pn = " + root.getPn() + " dn= " + root.getDn());
		res.setIteration(i);
		if (root.isLost()) {
			res.setTruth(false);
			return false;
		}
		if (root.isWin()) {
			res.setTruth(true);
			return true;
		}
		
		res.setIteration(maxT + 1);
		return false;
	}

}
