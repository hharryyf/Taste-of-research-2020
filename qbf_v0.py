import sys
def fix(st, pt, val):
    return st.replace(pt, val)

def solve(q, expr):
    if len(q) == 0:
        return eval(expr)
    lst = q[-1]
    curr = []
    for v in q:
        curr.append(v)
    curr.pop()
    s1, s2 = fix(expr, lst[1], "0"), fix(expr, lst[1], "1")
    if lst[0] == "all":
        return solve(curr, s1) and solve(curr, s2) 
    return solve(curr, s1) or solve(curr, s2)

argc = len(sys.argv)
if argc != 3:
    print("usage quantifiers + expression")
    exit(0)

x = sys.argv[1].replace(",", " ")
Q1 = x.split()
B = sys.argv[2]
Q = []
for i in range(0, len(Q1), 2):
    c1 = Q1[i]
    c2 = Q1[i+1]
    Q.append([c1, c2])
Q.reverse()
print(solve(Q, B))