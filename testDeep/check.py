import sys, os, getopt
import csv
import time
import random
li = []
for i in range(0, 10):
    v = random.randint(0, 1000)
    li.append(str(v) + "gttt_1_1_000111_3x3_b.qdimacs")
    os.system("cat gttt_1_1_000111_3x3_b.qdimacs " + " | bule_win64 shuffle --seed=" + str(v) + " --polarity > " + str(v) + "gttt_1_1_000111_3x3_b.qdimacs")
cmd = "java -jar "
progs = ["deepv1.jar 5 1 1", "deepv1.jar 5 1 2", "deepv1.jar 5 1 3", "deepv1.jar 5 1 4", \
        "deepv1.jar 5 2 1", "deepv1.jar 5 2 2", "deepv1.jar 5 2 3", "deepv1.jar 5 2 4", \
        "deepv1.jar 5 3 1", "deepv1.jar 5 3 2", "deepv1.jar 5 3 3", "deepv1.jar 5 3 4", \
        "deepv1.jar 5 4 1", "deepv1.jar 5 4 2", "deepv1.jar 5 4 3", "deepv1.jar 5 4 4"]
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
