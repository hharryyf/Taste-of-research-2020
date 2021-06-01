package qbfsolver;

public class BruteForceIterative implements Solver {
	@Override
	public boolean solve(CnfExpression f) {
		BJNode root = new BJNode();
		BJNode curr = root;
		curr.eval = f.evaluate();
		while (curr != null) {
			while (curr.eval != -1) {
				if (curr.parent == null) {
					if (curr.eval == 1) return true;
					return false;
				}
				curr = curr.parent;
				f.undo();
				if (curr.branch.isMax()) {
					if ((curr.left != null && curr.left.eval == 1) 
						|| (curr.right != null && curr.right.eval == 1)) {
						curr.eval = 1;
						if (curr.left != null) curr.left.parent = null;
						if (curr.right != null) curr.right.parent = null;
						curr.left = null;
						curr.right = null;
					} else if (curr.right != null && curr.right.eval == 0) {
						curr.eval = 0;
						if (curr.left != null) curr.left.parent = null;
						if (curr.right != null) curr.right.parent = null;
						curr.left = null;
						curr.right = null;
					}
				} else {
					if ((curr.left != null && curr.left.eval == 0) 
							|| (curr.right != null && curr.right.eval == 0)) {
						curr.eval = 0;
						if (curr.left != null) curr.left.parent = null;
						if (curr.right != null) curr.right.parent = null;
						curr.left = null;
						curr.right = null;
					} else if (curr.right != null && curr.right.eval == 1) {
						curr.eval = 1;
						if (curr.left != null) curr.left.parent = null;
						if (curr.right != null) curr.right.parent = null;
						curr.left = null;
						curr.right = null;
					}
				}
			}
			
			if (curr.branch == null) {
				curr.branch = f.peekfreq(1, f.peek().isMax()).get(0);
			}
			
			if (curr.left == null) {
				f.set(curr.branch.getVal());
				f.simplify();
				f.commit();
				curr.left = new BJNode();
				curr.left.parent = curr;
				curr = curr.left;
				curr.eval = f.evaluate();
			} else {
				f.set(-curr.branch.getVal());
				f.simplify();
				f.commit();
				if (curr.left != null) curr.left.parent = null;
				curr.left = null;
				curr.right = new BJNode();
				curr.right.parent = curr;
				curr = curr.right;
				curr.eval = f.evaluate();
			}
		}
		
		
		
		return false;
	}
	/*
	private void dfs(BJNode root) {
		System.out.println("curr");
		if (root.left != null) {
			System.out.println("left");
			dfs(root.left);
		} 
		
		if (root.right != null) {
			System.out.println("right");
			dfs(root.right);
		}
	}
    */
}
