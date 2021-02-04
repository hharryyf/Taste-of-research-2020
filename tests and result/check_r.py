import sys, os, getopt
import csv
import time
import random
li = []
for i in range(0, 10):
    v = random.randint(0, 1000)
    li.append(str(v) + "gttt_1_1_000111_3x3_torus_b.qdimacs")
    os.system("cat gttt_1_1_000111_3x3_torus_b.qdimacs " + " | bule_win64 shuffle --seed=" + str(v) + " --polarity > " + str(v) + "gttt_1_1_000111_3x3_torus_b.qdimacs")
cmd = "java -jar "
progs = ["deepR.jar 5 1 1 0.0", "deepR.jar 5 1 1 0.1", "deepR.jar 5 1 1 0.2", "deepR.jar 5 1 1 0.3", \
        "deepR.jar 5 1 1 0.4", "deepR.jar 5 1 1 0.5", "deepR.jar 5 1 1 0.6", "deepR.jar 5 1 1 0.7", \
        "deepR.jar 5 1 1 0.8", "deepR.jar 5 1 1 0.9", "deepR.jar 5 1 1 1.0"]
for prog in progs:
    tol = 0
    iter = 0
    for file in li:
        start_time = time.time()
        os.system(cmd + prog + " < " + file + " > out.txt")
        end_time = time.time()
        tol += end_time - start_time
        with open('out.txt', 'r') as f:
            lines = f.read().splitlines()
            last_line = lines[-1]
            ls = last_line.split()
            iter += int(ls[1])
    print("program: " + prog)
    print("Average time= ", round(tol / 10, 2), " average iteration= ", round(iter / 10, 2))
for f in li:
    os.system("rm " + f)
os.system("rm out.txt")
