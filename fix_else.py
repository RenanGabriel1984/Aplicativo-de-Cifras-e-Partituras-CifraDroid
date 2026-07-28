import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

new_lines = []
for i in range(len(lines)):
    line = lines[i]
    if line.startswith("        }"):
        # Check if next line is dedented or something indicating an else block was here
        # Or look for unbalanced braces.
        pass
