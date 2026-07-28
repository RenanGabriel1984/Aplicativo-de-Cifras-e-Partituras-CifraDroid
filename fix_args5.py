import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "com.example.ui.layout.StageLayoutType.Compact" in line:
        lines[i] = line.replace("com.example.ui.layout.StageLayoutType.Compact", "com.example.ui.layout.StageLayoutType.PHONE")

with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
