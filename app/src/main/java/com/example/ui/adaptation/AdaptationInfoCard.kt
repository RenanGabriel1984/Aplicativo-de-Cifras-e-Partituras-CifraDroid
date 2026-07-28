package com.example.ui.adaptation

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun AdaptationInfoCard(
    key: String,
    capo: Int,
    category: String,
    onEditKey: () -> Unit,
    onEditCapo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Tom", style = MaterialTheme.typography.labelMedium)
                    Text(text = key, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = onEditKey, enabled = false) {
                    Text("Editar")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Capotraste", style = MaterialTheme.typography.labelMedium)
                    Text(text = if (capo > 0) "Casa $capo" else "Nenhum", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = onEditCapo, enabled = false) {
                    Text("Editar")
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Column {
                Text(text = "Categoria", style = MaterialTheme.typography.labelMedium)
                Text(text = category, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
