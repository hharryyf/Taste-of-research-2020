import sys, os, getopt
import csv
import time
import random
li = ["CHAIN13v.14.qdimacs.gz", "CHAIN12v.13.qdimacs.gz", "gttt_1_1_000111_3x3_b.qdimacs", "gttt_1_1_000111_3x3_torus_b.qdimacs", "gttt_2_2_0010_4x4_torus_w-0.qdimacs"]
cmd = "java -jar "
progs = ["speed.jar", "speed.jar N"]
for prog in progs:
    print("program: " + prog)
    for file in li:
        tol = 0
        start_time = time.time()
        os.system(cmd + prog + " < " + file + " > out.txt")
        end_time = time.time()
        tol += end_time - start_time
        print("Instance: " + file)
        print("Average time= ", tol)
os.system("rm out.txt")
