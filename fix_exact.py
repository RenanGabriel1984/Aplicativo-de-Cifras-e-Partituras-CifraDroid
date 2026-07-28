import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

fix_list = [314, 374, 407, 415, 419, 482, 777, 848, 918, 938, 1025, 1093, 1675, 1715, 1740, 1835, 1845, 1958, 2025, 2301, 2371, 2380, 2437, 2450]

for i in fix_list:
    line_idx = i - 1
    # Replace the last } with } else {
    idx = lines[line_idx].rfind("}")
    if idx != -1:
        lines[line_idx] = lines[line_idx][:idx] + "} else {" + lines[line_idx][idx+1:]

with open(sys.argv[1], "w") as f:
    f.writelines(lines)
