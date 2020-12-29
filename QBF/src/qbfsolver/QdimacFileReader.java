package qbfsolver;

import java.util.Scanner;

public class QdimacFileReader {
	public CnfExpression read() {
		Scanner sc = new Scanner(System.in);
		String first = sc.nextLine();
		first = first.trim();
		String[] s = first.split("\\s+");
		int n = Integer.valueOf(s[2]);
		int m = Integer.valueOf(s[3]);
		CnfExpression ret = new OptimizedFormula(n);
		int i;
		while (m > 0) {
			first = sc.nextLine();
			first = first.trim();
			s = first.split("\\s+");
			if (s[0].charAt(0) == 'e') {
				for (i = 1; i < s.length; ++i) {
					int val = Integer.valueOf(s[i]);
					if (val != 0) {
						Quantifier q = new Quantifier(true, val);
						ret.addquantifier(q);
					}
				}
			} else if (s[0].charAt(0) == 'a') {
				for (i = 1; i < s.length; ++i) {
					int val = Integer.valueOf(s[i]);
					if (val != 0) {
						Quantifier q = new Quantifier(false, val);
						ret.addquantifier(q);
					}
				}
			} else {
				Disjunction c = new Disjunction();
				for (i = 0 ; i < s.length; ++i) {
					if (Integer.valueOf(s[i]) != 0) {
						c.add(Integer.valueOf(s[i]));
					}
				}
				
				if (!c.isEmpty()) {
					ret.addcnf(c);
				} else {
					ret.setSatisfied(false);
				}
				m--;
			}
		}
		sc.close();
		ret.normalize();
		return ret;
	}
}

/*
 * 
p cnf 4 2
e 1 2 3 4 0
-1 2 0
2 -3 -4 0

  p cnf 4 2
  -1 2 0
 2 -3 -4 0
 
 
 p cnf 5 9
 a 1 0
 e 2 0
 a 3 0
 e 4 5 0
 1 3 4 0
 -1 3 4 0
 1 -4 -5 0
 -1 2 5 0
 1 -3 4 -5 0
 -1 3 -4 0
 -1 -2 -3 -5 0
 1 -4 0
 3 -2 1 0
 
 * */