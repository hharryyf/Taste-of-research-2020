import sys, os, getopt
import csv
import time
cmd = "java -jar "
progs = [["first_qbfv5.jar", "PNS with stack and mobility init"], ["first_qbfv5.jar 1", "PNS"], ["first_qbfv5.jar 2", "PNS with stack"], ["first_qbfv5.jar 3", "PNS with mobility"]]
args = ["gttt_1_1_000111_3x3_b.qdimacs", "gttt_1_1_000111_3x3_torus_b.qdimacs"]
row_list = [["QBF_instance", "version", "time", "result", "iterations"]]

for prog in progs:
    for file in args:
        print("run QDIMAC file " + file + " with program " + prog[1])
        tol = 0
        start_time = time.time()
        os.system(cmd + prog[0] + " < " + file + " > out.txt")
        end_time = time.time()
        tol = end_time - start_time
        lst = [file, prog[1], str(tol)]
        if (tol >= 900000):
            lst[2] = "time limit exceeded"
        with open('out.txt', 'r') as f:
            lines = f.read().splitlines()
            last_line = lines[-1]
            ls = last_line.split()
            print(ls)
            lst.append(ls[0])
            lst.append(ls[1])        
        row_list.append(lst)

with open('result_PNS_dtopt.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    writer.writerows(row_list)
os.remove("out.txt")