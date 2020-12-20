package qbfsolver;

public interface TreeNode {
	public boolean isWin();
	public boolean isLost();
	public void expansion();
	public TreeNode MPN();
	public void backpropagation();
	public TreeNode getParent();
}
