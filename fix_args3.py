import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "com.example.ui.layout.StageLayoutType.COMPACT" in line:
        lines[i] = line.replace("com.example.ui.layout.StageLayoutType.COMPACT", "com.example.ui.layout.StageLayoutType.Compact")
    if "com.example.ui.screens.HudState.FULL" in line:
        lines[i] = line.replace("com.example.ui.screens.HudState.FULL", "com.example.ui.screens.HudState.Visible")

with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
