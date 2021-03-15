package qbfsolver;

import java.util.Scanner;

public class QdimacFileReader {
	public CnfExpression read(int type) {
		Scanner sc = new Scanner(System.in);
		String first = sc.nextLine();
		first = first.trim();
		String[] s = first.split("\\s+");
		int n = Integer.valueOf(s[2]);
		int m = Integer.valueOf(s[3]);
		CnfExpression ret;
		if (type == 1) {
			ret = new DataStructureOptimizedFormula(n);
		} else {
			ret = new PersistentFormula(n, m);
		}
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
				// Disjunction c = new DisjunctionDefault();
				Disjunction c;
				if (type == 0) {
					c = new PersistentClause();
				} else {
					c = new DisjunctionDefault();
				}
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
