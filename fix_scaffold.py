import sys
with open(sys.argv[1], 'r') as f:
    lines = f.readlines()

new_lines = []
for i, line in enumerate(lines):
    if 2068 <= i <= 2074:
        pass
    else:
        new_lines.append(line)

insertion = [
    "    ) { innerPadding ->\n",
    "        if (songCharts.isEmpty()) {\n",
    "            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {\n",
    "                Text(\"Nenhuma música encontrada.\", color = MaterialTheme.colorScheme.onSurface)\n",
    "            }\n",
    "        } else {\n"
]
new_lines = new_lines[:2068] + insertion + new_lines[2068:]

with open(sys.argv[1], 'w') as f:
    f.writelines(new_lines)
