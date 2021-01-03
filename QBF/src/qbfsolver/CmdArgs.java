package qbfsolver;

public class CmdArgs {
	private int type = 0;
	public CmdArgs() {
		
	}
	
	public CmdArgs(int type) {
		this.type = type;
	}
	
	public int getType() {
		return this.type;
	}
	
	public void setType(int val) {
		this.type = val;
	}
}
