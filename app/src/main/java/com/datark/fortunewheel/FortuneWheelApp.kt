package com.datark.fortunewheel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FortuneWheelApp() {
    val labels = remember {
        mutableStateListOf("1", "2", "3", "4", "5", "6", "7", "8")
    }
    var showSettings by remember { mutableStateOf(false) }
    var lastResult by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Koło Fortuny") },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Ustawienia")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Przeciągnij koło palcem lub stuknij w środek, aby zakręcić",
                style = MaterialTheme.typography.bodyMedium
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                contentAlignment = Alignment.Center
            ) {
                FortuneWheel(
                    labels = labels.toList(),
                    onSpinFinished = { index -> lastResult = labels.getOrNull(index) },
                    modifier = Modifier.fillMaxSize()
                )
            }

            lastResult?.let { result ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Wynik",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Text(
                            text = result,
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }

    if (showSettings) {
        WheelSettingsDialog(
            labels = labels,
            onDismiss = { showSettings = false }
        )
    }
}

@Composable
private fun WheelSettingsDialog(
    labels: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    onDismiss: () -> Unit
) {
    var countText by remember { mutableStateOf(labels.size.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Konfiguracja koła") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = countText,
                        onValueChange = { value ->
                            countText = value.filter { it.isDigit() }.take(2)
                        },
                        label = { Text("Liczba pól") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                    Button(onClick = {
                        val n = countText.toIntOrNull()?.coerceIn(2, 24) ?: labels.size
                        resizeLabels(labels, n)
                        countText = n.toString()
                    }) {
                        Text("Zastosuj")
                    }
                }

                Text("Etykiety pól", style = MaterialTheme.typography.titleSmall)

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(labels) { index, label ->
                        OutlinedTextField(
                            value = label,
                            onValueChange = { labels[index] = it },
                            label = { Text("Pole ${index + 1}") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Gotowe")
            }
        }
    )
}

private fun resizeLabels(
    labels: androidx.compose.runtime.snapshots.SnapshotStateList<String>,
    target: Int
) {
    while (labels.size < target) {
        labels.add((labels.size + 1).toString())
    }
    while (labels.size > target) {
        labels.removeAt(labels.size - 1)
    }
}
