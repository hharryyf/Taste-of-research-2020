package qbfsolver;

import java.util.Stack;

public class DeepPNS implements Solver {
	
	private int maxT = 1000000;
	public DeepPNS() {
		
	}
	
	public DeepPNS(int T) {
		this.maxT = T;
	}

	@Override
	public boolean solve(CnfExpression f) {
		// System.out.println(f);
		DeepPnNode root = new DeepPnNode(f, 1), curr = root;
		int i = 0, tolvisited = 0;
		Stack<CnfExpression> stk = new Stack<CnfExpression>();
		while (i <= this.maxT && !root.isSolved()) {
			if (i % 1000 == 0) {
				System.out.println("Iteration #" + i + " pn = " + root.getPn() + " dn= " + root.getDn());
			}
					
			if (stk.empty()) {
				stk.push(f.duplicate());
			}
					
			CnfExpression fp = stk.peek().duplicate();
			if (curr == null) curr = root;
			DeepPnNode it;
			while (true) {
				// System.out.println(fp);
				it = curr.MPN(fp);
				if (it == null) break;
				stk.push(fp.duplicate());
				curr = it;
				tolvisited++;
			}
			curr.expansion(fp);
			while (curr != null) {
				int pn = curr.getPn(), dn = curr.getDn();
				curr.backpropagation();
				if (pn == curr.getPn() && dn == curr.getDn()) break;
				curr = curr.getParent();
				stk.pop();
				tolvisited++;
			}
					
			i++;
		}
		Result res = ResultGenerator.getInstance();
		System.out.println("Iteration " + i + " pn = " + root.getPn() + " dn= " + root.getDn());
		System.out.println("Tolvisited = " + tolvisited);
		res.setIteration(i);
		if (root.isWin()) {
			res.setTruth(true);
			return true;
		}
		
		if (root.isLost()) {
			res.setTruth(false);
			return false;
		}
		
		res.setIteration(maxT + 1);
		return false;
	}

}
