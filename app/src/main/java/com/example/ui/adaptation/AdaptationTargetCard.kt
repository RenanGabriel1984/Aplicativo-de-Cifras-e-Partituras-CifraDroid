package com.example.ui.adaptation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdaptationTargetCard(
    targetKey: String,
    targetCapo: Int,
    profileName: String,
    onKeyChange: (String) -> Unit,
    onCapoChange: (Int) -> Unit,
    onProfileNameChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configurar Nova Adaptação",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            OutlinedTextField(
                value = profileName,
                onValueChange = onProfileNameChange,
                label = { Text("Nome do Perfil") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = targetKey,
                    onValueChange = onKeyChange,
                    label = { Text("Novo Tom") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = targetCapo.toString(),
                    onValueChange = { onCapoChange(it.toIntOrNull() ?: 0) },
                    label = { Text("Novo Capo") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
