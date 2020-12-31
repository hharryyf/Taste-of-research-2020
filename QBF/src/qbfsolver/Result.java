package qbfsolver;

public class Result {
	private String truth;
	private int iterations;
	public Result() {
		this.truth = new String("UNSOLVED");
		this.iterations = 1000000;
	}
	
	public void setIteration(int it) {
		this.iterations = it;
	}
	
	public void setTruth(boolean val) {
		if (val) {
			this.truth = new String("SAT");
		} else {
			this.truth = new String("UNSAT");
		}
	}
	
	public String toString() {
		return truth + " " + iterations;
	}
}
