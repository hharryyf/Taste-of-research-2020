package qbfsolver;

public class PNS implements Solver {
	private int maxT = 1000000;
	public PNS() {
		
	}
	
	public PNS(int maxT) {
		this.maxT = maxT;
	}
	
	@Override
	public boolean solve(CnfExpression f) {
		PNSNode root = new PNSNode(f);
		int i = 0;
		while (i <= this.maxT && !root.isWin() && !root.isLost()) {
			if (i % 1000 == 0) {
				System.out.println("Iteration #" + i + " pn = " + root.getPn() + " dn= " + root.getDn());
			}
			PNSNode curr = root, it;
			CnfExpression fp = f.duplicate();
			while (true) {
				// System.out.println(fp);
				it = curr.MPN(fp);
				if (it == null) break;
				curr = it;
			}
			
			curr.expansion(fp);
			while (curr != null) {
				curr.backpropagation();
				curr = curr.getParent();
				f.undo();
			}
			
			i++;
		}
		Result res = ResultGenerator.getInstance();
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

/*
 * no formula if the node is expanded, 
 * start with the formula, only part of the formula
 * 
 * */
