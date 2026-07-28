package com.example.ui.adaptation

import androidx.compose.material3.*
import androidx.compose.runtime.Composable

@Composable
fun AdaptationConfirmationDialog(
    profileName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Criar Interpretação") },
        text = { 
            Text("Deseja criar a versão '$profileName'? O documento original será preservado e uma nova adaptação será gerada para o seu Workspace.") 
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
