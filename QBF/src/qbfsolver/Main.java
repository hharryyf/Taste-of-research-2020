package qbfsolver;

public class Main {

	public static void main(String[] args) {
		QdimacFileReader rd = new QdimacFileReader();
		CnfExpression f = rd.read();
		Solver s = new PNSv2();
		System.out.println(s.solve(f));
	}

}
