package qbfsolver;

public class PNS implements Solver {

	@Override
	public boolean solve(Formula f) {
		TreeNode root = new PNSNode(f);
		int maxT = 5000000, i = 0;
		while (i <= maxT && !root.isWin() && !root.isLost()) {
			TreeNode curr = root;
			while (curr.MPN() != null) {
				curr = curr.MPN();
			}
			
			curr.expansion();
			while (curr != null) {
				curr.backpropagation();
				curr = curr.getParent();
			}
			
			i++;
		}
		
		if (root.isLost()) return false;
		if (root.isWin()) return true;
		System.err.println("Failed to get the answer within 5000000 iterations!");
		return false;
	}

}
