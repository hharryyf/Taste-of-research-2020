import sys, os, getopt
import csv
import time
import random
li = []
testcase = 200
for i in range(0, testcase):
    v = random.randint(0, 1000)
    li.append(str(v) + "gttt_1_1_000111_3x3_b.qdimacs")
    os.system("cat gttt_1_1_000111_3x3_b.qdimacs " + " | bule_win64 shuffle --seed=" + str(v) + " --polarity > " + str(v) + "gttt_1_1_000111_3x3_b.qdimacs")
cmd = "java -jar "
progs = ["deepR.jar 5 1 1 0.25", "deepR.jar 5 1 2 0.25", "deepR.jar 5 4 3 0.25", "deepR.jar 5 4 4 0.25", \
        "deepR.jar 5 2 1 0.25", "deepR.jar 5 2 2 0.25", "deepR.jar 5 2 4 0.25", "deepR.jar 5 2 3 0.25", \
        "deepR.jar 5 3 1 0.25", "deepR.jar 5 3 2 0.25", "deepR.jar 5 4 2 0.25"]
tol = 1
for file in li:
    idx = random.randint(0, len(progs))
    if idx == len(progs):
        idx -= 1
    prog = progs[idx]
    os.system(cmd + prog + " < " + file + " > out.txt")
    pt = 1
    with open('out.txt', 'r') as f:
        lines = f.read().splitlines()
        last_line = lines[-1]
        ls = last_line.split()
        if ls[0] != "SAT":
            pt = 0
    if pt == 0:
        print("wrong answer on test case " + str(tol))
        break
    else:
        print("pass test case " + str(tol))
    tol += 1
if tol == testcase + 1:
    print("pass all random test")
else:
    print("wrong answer")
for f in li:
    os.system("rm " + f)
os.system("rm out.txt")
