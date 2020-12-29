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



