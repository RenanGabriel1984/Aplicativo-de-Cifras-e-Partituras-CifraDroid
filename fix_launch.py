import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if i >= 841 and i <= 843:
        pass
    else:
        new_lines.append(line)

# I'll just insert the correct lines
new_lines.insert(841, '            }\n')
new_lines.insert(842, '        }\n')

with open(sys.argv[1], "w") as f:
    f.writelines(new_lines)
