package io.methodra.app.today

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.methodra.app.design.MethodraColors
import io.methodra.app.onboarding.EvidenceChip

private val recoveryReasons = listOf(
    "Ability", "Opportunity", "Motivation", "Task size", "Timing", "Unrealistic rule"
)

@Composable
fun TodayScreen(
    onOpenFocus: () -> Unit,
    viewModel: TodayViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var checkInOpen by remember { mutableStateOf(false) }
    var rating by remember { mutableIntStateOf(3) }
    var automaticity by remember { mutableIntStateOf(3) }
    var recoveryReason by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val method = state.method
    val active = state.active

    LaunchedEffect(state.daily?.id) {
        rating = state.daily?.checkInRating ?: 3
        automaticity = state.daily?.automaticityRating ?: 3
        recoveryReason = state.daily?.recoveryReason.orEmpty()
        note = state.daily?.note.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MethodraColors.Obsidian)
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Today", style = MaterialTheme.typography.headlineLarge)
        Text("One protocol. A reason for every action.", color = MethodraColors.Muted)

        if (method == null || active == null) {
            EmptyCard("No active protocol", "Restart onboarding from Settings to choose a method.")
            return@Column
        }

        ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(method.name.uppercase(), color = MethodraColors.Amber, style = MaterialTheme.typography.labelLarge)
                    EvidenceChip(method.evidence.level)
                }
                Text(active.desiredOutcome, style = MaterialTheme.typography.titleLarge)
                Text(method.shortExplanation, color = MethodraColors.Muted)
                if (active.matchReasonsText.isNotBlank()) {
                    Text("Why this method", fontWeight = FontWeight.SemiBold)
                    active.matchReasonsText.lineSequence().filter { it.isNotBlank() }.forEach { reason ->
                        Text("• $reason", color = MethodraColors.MineralBlue, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (active.setupSummaryText.isNotBlank()) {
                    Text("Your setup", fontWeight = FontWeight.SemiBold)
                    active.setupSummaryText.lineSequence().filter { it.isNotBlank() }.forEach { line ->
                        Text(line, color = MethodraColors.Muted, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                HorizontalDivider(color = MethodraColors.ElevatedStone)
                method.steps.take(3).forEachIndexed { index, step ->
                    val done = index in state.completedSteps
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleStep(index) }.padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Checkbox(checked = done, onCheckedChange = { viewModel.toggleStep(index) })
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(step.title, fontWeight = FontWeight.SemiBold)
                            Text(step.instruction, color = MethodraColors.Muted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Surface(color = MethodraColors.ElevatedStone, shape = RoundedCornerShape(14.dp)) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Difficult day version", color = MethodraColors.MineralBlue, fontWeight = FontWeight.SemiBold)
                        Text(method.minimumVersion)
                    }
                }
            }
        }

        ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
            Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Protected focus", fontWeight = FontWeight.SemiBold)
                    Text(method.focusRule ?: "Use a focus block only when it serves the method.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodyMedium)
                }
                FilledIconButton(onClick = onOpenFocus) { Icon(Icons.Default.PlayArrow, contentDescription = "Open focus") }
            }
        }

        OutlinedButton(onClick = { checkInOpen = true }, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.daily?.checkInRating == null) "Short check-in" else "Edit today's check-in")
        }
        Spacer(Modifier.height(12.dp))
    }

    if (checkInOpen) {
        AlertDialog(
            onDismissRequest = { checkInOpen = false },
            title = { Text("How usable was the protocol?") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("$rating / 5", color = MethodraColors.Amber)
                    Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt().coerceIn(1, 5) }, valueRange = 1f..5f, steps = 3)

                    if (method?.id == "context_anchor") {
                        Text("How automatic did the action feel?", fontWeight = FontWeight.SemiBold)
                        Text("Track automaticity separately from completion; there is no streak reset.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
                        Text("$automaticity / 5", color = MethodraColors.MineralBlue)
                        Slider(value = automaticity.toFloat(), onValueChange = { automaticity = it.toInt().coerceIn(1, 5) }, valueRange = 1f..5f, steps = 3)
                    }

                    if (rating <= 2) {
                        Text("What got in the way?", fontWeight = FontWeight.SemiBold)
                        Text("Choose the closest cause so the next review can adapt the rule instead of punishing the miss.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
                        recoveryReasons.forEach { reason ->
                            FilterChip(
                                selected = recoveryReason == reason,
                                onClick = { recoveryReason = reason },
                                label = { Text(reason) }
                            )
                        }
                    }

                    OutlinedTextField(note, { note = it }, label = { Text("Context note (optional)") })
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.checkIn(
                        rating = rating,
                        note = note,
                        automaticity = if (method?.id == "context_anchor") automaticity else null,
                        recoveryReason = if (rating <= 2) recoveryReason else ""
                    )
                    checkInOpen = false
                }) { Text("Save") }
            },
            dismissButton = { TextButton(onClick = { checkInOpen = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EmptyCard(title: String, body: String) {
    ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
        Column(Modifier.padding(20.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(6.dp))
            Text(body, color = MethodraColors.Muted)
        }
    }
}
