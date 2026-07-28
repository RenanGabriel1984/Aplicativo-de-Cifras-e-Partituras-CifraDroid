import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

for i in range(len(lines)):
    line = lines[i]
    if line.rstrip().endswith("}"):
        this_indent = len(line) - len(line.lstrip())
        
        # find next non-empty line
        j = i + 1
        while j < len(lines) and lines[j].strip() == "":
            j += 1
            
        if j < len(lines):
            next_line = lines[j]
            next_indent = len(next_line) - len(next_line.lstrip())
            
            # If next line is more indented, and does not start with } or catch etc
            if next_indent > this_indent and not next_line.lstrip().startswith("}"):
                # We found a broken else!
                # Replace the LAST } on this line with } else {
                idx = line.rfind("}")
                lines[i] = line[:idx] + "} else {" + line[idx+1:]

with open(sys.argv[1], "w") as f:
    f.writelines(lines)
