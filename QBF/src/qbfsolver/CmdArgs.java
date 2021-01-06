package qbfsolver;

public class CmdArgs {
	private int type = 0;
	private int bf = 1;
	public CmdArgs() {
		
	}
	
	public CmdArgs(int type) {
		this.type = type;
	}
	
	public int getBf() {
		return this.bf;
	}
	
	public void setBf(int val) {
		this.bf = val;
		this.bf = Math.max(bf, 1);
		this.bf = Math.min(4, this.bf);
	}
	
	public int getType() {
		return this.type;
	}
	
	public void setType(int val) {
		this.type = val;
	}
}
