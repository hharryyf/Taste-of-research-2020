package qbfsolver;

public class Main {

	public static void main(String[] args) {
		QdimacFileReader rd = new QdimacFileReader();
		Formula f = rd.read();
		Solver s = new PNS();
		System.out.println(s.solve(f));
	}

}
