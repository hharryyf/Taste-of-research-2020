import sys, os, getopt
import time
maxT = 3
if sys.argv == 2:
    maxT = int(sys.argv[1])
cmd = "java -jar "
progs = ["first_qbfv2.jar"]
args = ["gttt_1_1_000111_3x3_b.qdimacs", "gttt_1_1_000111_3x3_torus_b.qdimacs"]
for prog in progs:
    for file in args:
        print("run QDIMAC file " + file + " with program " + prog)
        tol = 0
        for i in range(0, maxT):
            start_time = time.time()
            os.system(cmd + prog + " < " + file + " > Nul")
            end_time = time.time()
            tol += end_time - start_time
        print("average time used in " + str(maxT) + " iterations= " + str(round(tol / maxT, 2)) + " s")