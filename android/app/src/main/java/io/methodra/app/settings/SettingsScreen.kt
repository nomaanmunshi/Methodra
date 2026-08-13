package io.methodra.app.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.methodra.app.design.MethodraColors

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val reduceMotion by viewModel.reduceMotion.collectAsStateWithLifecycle()
    val haptics by viewModel.haptics.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var deleteDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().background(MethodraColors.Obsidian).safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Back") }
        Text("Settings", style = MaterialTheme.typography.headlineLarge)

        SettingSwitch("Reduced motion", "Use simpler state changes instead of cinematic motion where possible.", reduceMotion, viewModel::setReduceMotion)
        SettingSwitch("Haptics", "Allow restrained tactile feedback during onboarding and deliberate interactions.", haptics, viewModel::setHaptics)

        HorizontalDivider(color = MethodraColors.ElevatedStone)
        Text("Privacy & data", style = MaterialTheme.typography.titleLarge)
        Text("Detailed app-usage events remain local. Backend synchronization is optional and not required for core use.", color = MethodraColors.Muted)

        OutlinedButton(
            onClick = {
                viewModel.export { json ->
                    val share = Intent(Intent.ACTION_SEND).apply {
                        type = "application/json"
                        putExtra(Intent.EXTRA_SUBJECT, "Methodra data export")
                        putExtra(Intent.EXTRA_TEXT, json)
                    }
                    context.startActivity(Intent.createChooser(share, "Export Methodra data"))
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Export local data as JSON") }

        OutlinedButton(onClick = viewModel::restartOnboarding, modifier = Modifier.fillMaxWidth()) { Text("Run onboarding again") }
        TextButton(onClick = { deleteDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Delete local Methodra data", color = MethodraColors.Danger) }

        Spacer(Modifier.height(24.dp))
        Text("Methodra does not diagnose or treat medical or psychological conditions. Focus restrictions are intentionally reversible.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
    }

    if (deleteDialog) {
        AlertDialog(
            onDismissRequest = { deleteDialog = false },
            title = { Text("Delete local data?") },
            text = { Text("This clears local protocol, focus, and trial history. This action cannot be undone unless you exported a copy.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteLocalData(); deleteDialog = false }) { Text("Delete", color = MethodraColors.Danger) }
            },
            dismissButton = { TextButton(onClick = { deleteDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingSwitch(title: String, body: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(body, color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
        }
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
