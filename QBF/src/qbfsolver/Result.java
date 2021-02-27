package qbfsolver;

public class Result {
	private String truth;
	private long iterations;
	private int nodecount;
	public Result() {
		this.truth = new String("UNSOLVED");
		this.iterations = 0;
		this.nodecount = 0;
	}
	
	public void setIteration(long it) {
		this.iterations = it;
	}
	
	public long getIteration() {
		return this.iterations;
	}
	
	public void setTruth(boolean val) {
		if (val) {
			this.truth = new String("SAT");
		} else {
			this.truth = new String("UNSAT");
		}
	}
	
	public void setNode() {
		this.nodecount++;
	}
	
	public String toString() {
		return truth + " " + iterations + " " + nodecount;
	}
}
