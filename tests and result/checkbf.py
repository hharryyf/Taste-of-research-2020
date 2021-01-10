import sys, os, getopt
import csv
import time
import random
cmd = "java -jar "
progs = ["fast.jar "]
cmdarg = [["4 1", "mobility initialization with stack", "2"], \
		  ["4 2", "mobility initialization with stack", "4"], \
		  ["4 3", "mobility initialization with stack", "8"], \
		  ["4 4", "mobility initialization with stack", "16"], \
	      ["2 1", "standard initialization with stack", "2"], \
	      ["2 2", "standard initialization with stack", "4"], \
	      ["2 3", "standard initialization with stack", "8"], \
	      ["2 4", "standard initialization with stack", "16"]]
args = [[[], "gttt_1_1_000111_3x3_b.qdimacs"], [[],"gttt_1_1_000111_3x3_torus_b.qdimacs"]]
row_list = [["QBF_instance", "max branching factor", "version", "time", "result", "iterations"]]

for i in range(0, 2):
    for j in range(0, 5):
        v = random.randint(0, 100)
        os.system("cat " + args[i][1] + " | bule_win64 shuffle --seed=" + str(v) + " --polarity > " + str(v) + args[i][1])
        args[i][0].append(str(v) + args[i][1])

for prog in progs:
    for c in cmdarg:
        for file in args:
            print("run QDIMAC file " + file[1] + " with program " + prog)
            tol, it, cnt = 0, 0, 0
            solved = "UNSOLVED"
            findbad = 0
            lst = [file[1], c[2], c[1]]
            for f in file[0]:
                start_time = time.time()
                os.system(cmd + prog + c[0] + " < " + f + " > out.txt")
                end_time = time.time()
                cnt += 1
                tol += end_time - start_time
                with open('out.txt', 'r') as f:
                    lines = f.read().splitlines()
                    last_line = lines[-1]
                    ls = last_line.split()
                    solved = ls[0]
                    if (ls[1] == 'NA'):
                        findbad = 1
                    else:
                        it += int(ls[1])        
            if findbad != 0:
                lst.append("> 900")
                lst.append("UNSOLVED")
                lst.append("> 1000000")
            else:
                lst.append(round(tol / cnt, 2))
                lst.append(solved)
                lst.append(round(it / cnt, 2))    
            row_list.append(lst)

with open('resultshuffle_bf.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    writer.writerows(row_list)
for file in args:
    for f in file[0]:
        os.system("rm " + f)
os.system("rm out.txt")
