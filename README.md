# Taste-of-research-2020

update 18/12/2020:

The brute force with optimized approch was implemented, now the program might early exit if the formula has already been 
proved or disproved.

update 21/12/2020:
Finish the first version of PNS search, tested correctly.

update 23/12/2020:
Some memory optimization was done, at most 4 formulas are stored in memory. Used strategy design pattern on the CnfExpression class,
get ready for implementing DPLL algorithm and unit propagation.

update 24/12/2020:
Implement a very brute force version of the unit-propagation algorithm, tested correctly and the performance of the solver is improved
significantly. 

update 28/12/2020:
Implement the second version of proof number search, in this version, the backtracking procedure would stop at the first node which
pn and dn are not changed. And magically, the run time for the gttt3x3 example was reduced to 1min instead of the origional 8min.

update 1/1/2021:
Implement a brute force version of the pure literal elimination and it can be seen that the number of iterations was reduced to 1/5.

update 3/1/2021:
Attempt the mobility initialization i.e. (2, 1) or (1, 2) instead of (1, 1). The number of iterations was halved, upload result_PNS.csv.

update 5/1/2021:
Implement the data structure optimization version of unit propagation and pure literal elimination, the algorithm has an 
armotized complexity of O(logn) when detecting unit clauses and pure literals.

update 6/1/2021:
Extend the PNSNode class that allows the branching factor to be more than 2, for performance consideration only 4, 8, 16 are allowed.

update 7/1/2021:
Run tests on shuffled qdimacs files, the result and we can tell the data structure optimization is extremely powerful.

