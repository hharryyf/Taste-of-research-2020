package qbfsolver;

public class ResultGenerator {
	private static Result ret = null;
	public static Result getInstance() {
		if (ret == null) {
			ret = new Result();
		}
		return ret;
	}
}
