import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if "com.example.ui.layout.StageLayoutType.COMPACT, androidx.compose.ui.unit.dp(600f), androidx.compose.ui.unit.dp(800f)" in line:
        lines[i] = line.replace("androidx.compose.ui.unit.dp(600f)", "600.androidx.compose.ui.unit.dp")
        lines[i] = lines[i].replace("androidx.compose.ui.unit.dp(800f)", "800.androidx.compose.ui.unit.dp")

with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
