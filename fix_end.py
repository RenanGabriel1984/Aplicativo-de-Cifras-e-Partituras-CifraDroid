import sys

with open(sys.argv[1], "r") as f:
    lines = f.readlines()

new_lines = []
skip = False
for i, line in enumerate(lines):
    if i == 1996: # 0-indexed, so 1997
        new_lines.append('        androidx.compose.material3.SnackbarHost(\n')
        new_lines.append('            hostState = snackbarHostState,\n')
        new_lines.append('            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp)\n')
        new_lines.append('        )\n')
        new_lines.append('        androidx.compose.animation.AnimatedVisibility(\n')
        new_lines.append('            visible = showPreparationWorkspace && preparationState != null,\n')
        new_lines.append('            enter = androidx.compose.animation.fadeIn(),\n')
        new_lines.append('            exit = androidx.compose.animation.fadeOut(),\n')
        new_lines.append('            modifier = Modifier.align(Alignment.Center).zIndex(100f)\n')
        new_lines.append('        ) {\n')
        new_lines.append('            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {\n')
        new_lines.append('                com.example.ui.preparation.PreparationWorkspaceScreen(\n')
        new_lines.append('                    state = preparationState!!,\n')
        new_lines.append('                    responsiveState = com.example.ui.theme.ResponsiveStageState(com.example.ui.theme.ResponsiveStage.COMPACT, 600, 800, false, false),\n')
        new_lines.append('                    modifier = Modifier.fillMaxSize()\n')
        new_lines.append('                )\n')
        new_lines.append('                androidx.compose.material3.Button(\n')
        new_lines.append('                    onClick = { showPreparationWorkspace = false },\n')
        new_lines.append('                    modifier = Modifier.align(Alignment.TopEnd).windowInsetsPadding(WindowInsets.safeDrawing).padding(16.dp)\n')
        new_lines.append('                ) {\n')
        new_lines.append('                    Text("Fechar")\n')
        new_lines.append('                }\n')
        new_lines.append('            }\n')
        new_lines.append('        }\n')
    elif 1996 <= i <= 2021:
        continue
    else:
        new_lines.append(line)

with open(sys.argv[1], "w") as f:
    f.writelines(new_lines)
