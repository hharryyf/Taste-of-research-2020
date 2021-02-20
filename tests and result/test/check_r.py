import sys, os, getopt
import csv
import time
import random
li = ["CHAIN12v.13_SAT_4000.qdimacs.gz"]
cmd = "java -jar "
progs = ["bchfeq.jar 5 1 3 0.15", "bchfeq.jar 0"]
for prog in progs:
    tol = 0
    for file in li:
        start_time = time.time()
        os.system(cmd + prog + " < " + file)
        end_time = time.time()
        tol = end_time - start_time
    print("program: " + prog)
    print("Time time= ", tol)
