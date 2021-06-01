package qbfsolver;

public class BJNode {
	protected Quantifier branch;
	protected Reason r = null;
    protected BJNode left = null, right = null, parent = null;
    protected int eval = -1;
    public BJNode() {
    	Result ret = ResultGenerator.getInstance();
    	ret.setIteration(ret.getIteration() + 1);
    }
}
