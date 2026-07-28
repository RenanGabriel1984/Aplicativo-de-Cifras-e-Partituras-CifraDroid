import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "hostState = snackbarHostState," in line:
        # Move the modifier back up
        break

# I will just write a specific sed script to swap the lines or rewrite them cleanly.
