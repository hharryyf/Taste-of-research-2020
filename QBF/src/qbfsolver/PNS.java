package qbfsolver;

public class PNS implements Solver {

	@Override
	public boolean solve(Formula f) {
		PNSNode root = new PNSNode(f);
		int maxT = 5000000, i = 0;
		while (i <= maxT && !root.isWin() && !root.isLost()) {
			PNSNode curr = root, it;
			Formula fp = f.duplicate();
			if (i % 1000 == 0) {
				System.out.println("Iteration #" + i + " pn = " + ((PNSNode) root).getPn() + " dn= " + ((PNSNode) root).getDn());
			}
			
			while (true) {
				it = curr.MPN(fp);
				if (it == null) break;
				curr = it;
			}
			
			curr.expansion(fp);
			while (curr != null) {
				curr.backpropagation();
				curr = curr.getParent();
			}
			
			i++;
		}
		
		System.out.println("Iteration " + i + " pn = " + ((PNSNode) root).getPn() + " dn= " + ((PNSNode) root).getDn());
		if (root.isLost()) return false;
		if (root.isWin()) return true;
		System.err.println("Failed to get the answer within 5000000 iterations!");
		return false;
	}

}

/*
 * no formula if the node is expanded, 
 * start with the formula, only part of the formula
 * 
 * */
