import sys, os, getopt
import csv
import time
cmd = "java -jar "
progs = ["first_qbfv3.jar N", "first_qbfv3.jar"]
args = ["gttt_1_1_000111_3x3_b.qdimacs", "gttt_1_1_000111_3x3_torus_b.qdimacs"]
row_list = [["QBF_instance", "version", "time"]]

for prog in progs:
    for file in args:
        print("run QDIMAC file " + file + " with program " + prog)
        tol = 0
        start_time = time.time()
        os.system(cmd + prog + " < " + file + " > Nul")
        end_time = time.time()
        tol = end_time - start_time
        lst = [file, prog, str(tol)]
        if (tol >= 900000):
            lst[2] = "time limit exceeded"        
        row_list.append(lst)

with open('result.csv', 'w', newline='') as file:
    writer = csv.writer(file)
    writer.writerows(row_list)
