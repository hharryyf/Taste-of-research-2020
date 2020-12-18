package qbfsolver;

public class Quantifier {
	private boolean isexist;
	private int val;
	public Quantifier(boolean isexist, int val) {
		this.isexist = isexist;
		this.val = val;
	}
	
	public boolean isMax() {
		return isexist;
	}
	
	public int getVal() {
		return val;
	}

	public Quantifier duplicate() {
		Quantifier q = new Quantifier(isexist, val);
		return q;
	}
	@Override
	public String toString() {
		if (isexist) {
			return "E " + val;
		} else {
			return "A " + val;
		}
	}
}
