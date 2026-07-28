import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()
lines[0] = "package com.example.ui.screens\nimport com.example.ui.theme.ResponsiveStage\nimport com.example.ui.theme.ResponsiveStageState\nimport android.content.res.Configuration\n"
with open(sys.argv[1], 'w') as f:
    f.writelines(lines)
