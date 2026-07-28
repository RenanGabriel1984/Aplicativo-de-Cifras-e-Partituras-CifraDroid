import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "com.example.ui.layout.ResponsiveStageState(com.example.ui.layout.ResponsiveStage.COMPACT, 600, 800, false, false)" in line:
        lines[i] = line.replace("com.example.ui.layout.ResponsiveStage.COMPACT, 600, 800, false, false", "com.example.ui.layout.StageLayoutType.COMPACT, androidx.compose.ui.unit.dp(600f), androidx.compose.ui.unit.dp(800f), com.example.ui.screens.HudState.FULL, true, false, false, false, false, false")

with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
