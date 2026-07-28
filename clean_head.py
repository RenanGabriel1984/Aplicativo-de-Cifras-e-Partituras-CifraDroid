import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

new_lines = [
    "package com.example.ui.screens\n",
    "import android.content.res.Configuration\n",
    "import androidx.compose.animation.AnimatedVisibility\n",
    "import androidx.compose.animation.fadeIn\n"
]

# Skip everything until we see an import starting with androidx.compose.animation...
start_idx = 0
for i, line in enumerate(lines):
    if "import androidx.compose.animation.AnimatedVisibility" in line:
        start_idx = i + 1
        break

new_lines.extend(lines[start_idx:])

with open(sys.argv[1], 'w') as f:
    f.writelines(new_lines)
