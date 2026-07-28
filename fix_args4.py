import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "com.example.ui.screens.HudState.Visible" in line:
        lines[i] = line.replace("com.example.ui.screens.HudState.Visible", "com.example.ui.screens.HudState.EXPANDED")

with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
