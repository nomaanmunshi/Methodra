package io.methodra.app.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.methodra.app.data.local.FocusRuleEntity
import io.methodra.app.data.local.FocusScheduleEntity
import io.methodra.app.design.MethodraColors
import io.methodra.app.domain.UsageApp
import kotlinx.coroutines.delay
import kotlin.math.max
import kotlin.math.roundToInt

private val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

@Composable
fun FocusScreen(viewModel: FocusViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var emergencyDialog by remember { mutableStateOf(false) }
    var scheduleDialog by remember { mutableStateOf(false) }
    var exitReason by remember { mutableStateOf("") }
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(state.activeSession?.id) {
        while (state.activeSession != null) {
            now = System.currentTimeMillis()
            if (now >= (state.activeSession?.plannedEndEpochMillis ?: Long.MAX_VALUE)) {
                viewModel.end()
                break
            }
            delay(1_000)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MethodraColors.Obsidian).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            Text("Focus", style = MaterialTheme.typography.headlineLarge)
            Text("Voluntary protection with a deliberate exit. Never impossible to bypass.", color = MethodraColors.Muted)
        }

        state.activeSession?.let { session ->
            item {
                val remainingSeconds = max(0L, (session.plannedEndEpochMillis - now) / 1_000L)
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MethodraColors.Amber)
                            Spacer(Modifier.width(8.dp))
                            Text("Protected block active", fontWeight = FontWeight.Bold)
                        }
                        Text("%02d:%02d".format(remainingSeconds / 60, remainingSeconds % 60), style = MaterialTheme.typography.headlineLarge)
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        OutlinedButton(onClick = { emergencyDialog = true }, modifier = Modifier.fillMaxWidth()) { Text("Emergency exit") }
                    }
                }
            }
        } ?: item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Start a protected block", style = MaterialTheme.typography.titleLarge)
                    Text("${state.durationMinutes} minutes", color = MethodraColors.Amber, fontWeight = FontWeight.SemiBold)
                    Slider(
                        value = state.durationMinutes.toFloat(),
                        onValueChange = { viewModel.setDuration((it / 5).roundToInt() * 5) },
                        valueRange = 15f..90f,
                        steps = 14
                    )
                    Button(onClick = { viewModel.start() }, enabled = state.rules.isNotEmpty(), modifier = Modifier.fillMaxWidth()) {
                        Text(if (state.rules.isEmpty()) "Select at least one distracting app" else "Start protection")
                    }
                }
            }
        }

        item {
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Permissions are explicit", fontWeight = FontWeight.Bold)
                    Text(
                        "Usage access is used only to calculate local usage totals. Accessibility protection observes foreground package changes only when you enable the service. Detailed usage events are not sent to the backend.",
                        color = MethodraColors.Muted
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = viewModel::openUsageSettings, modifier = Modifier.weight(1f)) { Text(if (state.hasUsageAccess) "Usage ✓" else "Usage access") }
                        OutlinedButton(onClick = viewModel::openAccessibilitySettings, modifier = Modifier.weight(1f)) { Text("Focus service") }
                    }
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Recurring protection", style = MaterialTheme.typography.titleLarge)
                    Text("Local wall-clock schedules. Overnight windows are supported.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
                }
                IconButton(onClick = { scheduleDialog = true }) { Icon(Icons.Default.Add, contentDescription = "Add focus schedule") }
            }
        }

        if (state.schedules.isEmpty()) {
            item { Text("No recurring schedules yet.", color = MethodraColors.Muted) }
        }
        items(state.schedules, key = { "schedule-${it.id}" }) { schedule ->
            ScheduleRow(
                schedule = schedule,
                onEnabled = { viewModel.setScheduleEnabled(schedule, it) },
                onDelete = { viewModel.deleteSchedule(schedule) }
            )
        }

        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Selected distractions", style = MaterialTheme.typography.titleLarge)
                IconButton(onClick = viewModel::refreshAppsAndUsage) { Icon(Icons.Default.Refresh, contentDescription = "Refresh") }
            }
        }

        val selectedPackages = state.rules.map { it.packageName }.toSet()
        val preferredApps = state.apps
            .sortedWith(compareByDescending<UsageApp> { it.packageName in selectedPackages }.thenBy { it.label })
            .take(40)
        items(preferredApps, key = { it.packageName }) { app ->
            val rule = state.rules.firstOrNull { it.packageName == app.packageName }
            AppRuleRow(app, rule, onToggle = { viewModel.toggleApp(app, it) }, onBudget = { minutes ->
                (rule ?: FocusRuleEntity(app.packageName, app.label)).let { viewModel.setBudget(it, minutes) }
            })
        }

        item {
            Text("Today's local usage", style = MaterialTheme.typography.titleLarge)
            if (!state.hasUsageAccess) {
                Text("Grant Usage Access to see local totals. Methodra does not upload detailed app events by default.", color = MethodraColors.Muted)
            }
        }
        items(state.usage.take(8), key = { "usage-${it.packageName}" }) { app ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(app.label)
                Text("${app.minutesToday} min", color = MethodraColors.Muted)
            }
        }
    }

    if (emergencyDialog) {
        AlertDialog(
            onDismissRequest = { emergencyDialog = false },
            title = { Text("Emergency exit") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Write a short reason. Misses are information, not punishment.")
                    OutlinedTextField(exitReason, { exitReason = it }, label = { Text("Reason") }, singleLine = true)
                }
            },
            confirmButton = {
                TextButton(enabled = exitReason.isNotBlank(), onClick = {
                    viewModel.emergencyExit(exitReason.trim())
                    exitReason = ""
                    emergencyDialog = false
                }) { Text("Exit block") }
            },
            dismissButton = { TextButton(onClick = { emergencyDialog = false }) { Text("Stay focused") } }
        )
    }

    if (scheduleDialog) {
        ScheduleDialog(
            onDismiss = { scheduleDialog = false },
            onSave = {
                viewModel.saveSchedule(it)
                scheduleDialog = false
            }
        )
    }
}

@Composable
private fun ScheduleRow(
    schedule: FocusScheduleEntity,
    onEnabled: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val start = formatMinute(schedule.startMinuteOfDay)
    val end = formatMinute(schedule.endMinuteOfDay)
    val days = dayLabels.mapIndexedNotNull { index, label -> if (schedule.daysMask and (1 shl index) != 0) label else null }.joinToString(" ")
    Surface(color = MethodraColors.Basalt, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(schedule.name, fontWeight = FontWeight.SemiBold)
                Text("$days · $start–$end", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            Switch(checked = schedule.enabled, onCheckedChange = onEnabled)
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = "Delete schedule") }
        }
    }
}

@Composable
private fun ScheduleDialog(onDismiss: () -> Unit, onSave: (FocusScheduleEntity) -> Unit) {
    var name by remember { mutableStateOf("Focus window") }
    var daysMask by remember { mutableIntStateOf(0b0011111) }
    var startHour by remember { mutableIntStateOf(9) }
    var endHour by remember { mutableIntStateOf(12) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Recurring focus schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text("Name") }, singleLine = true)
                Text("Days", fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    dayLabels.forEachIndexed { index, label ->
                        val bit = 1 shl index
                        FilterChip(
                            selected = daysMask and bit != 0,
                            onClick = { daysMask = daysMask xor bit },
                            label = { Text(label) }
                        )
                    }
                }
                Text("Start · %02d:00".format(startHour), color = MethodraColors.Amber)
                Slider(value = startHour.toFloat(), onValueChange = { startHour = it.roundToInt() }, valueRange = 0f..23f, steps = 22)
                Text("End · %02d:00".format(endHour), color = MethodraColors.Amber)
                Slider(value = endHour.toFloat(), onValueChange = { endHour = it.roundToInt() }, valueRange = 0f..23f, steps = 22)
                if (startHour == endHour) Text("Start and end must differ.", color = MaterialTheme.colorScheme.error)
            }
        },
        confirmButton = {
            TextButton(
                enabled = name.isNotBlank() && daysMask != 0 && startHour != endHour,
                onClick = {
                    onSave(
                        FocusScheduleEntity(
                            name = name.trim(),
                            daysMask = daysMask,
                            startMinuteOfDay = startHour * 60,
                            endMinuteOfDay = endHour * 60
                        )
                    )
                }
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun formatMinute(minute: Int): String = "%02d:%02d".format((minute / 60) % 24, minute % 60)

@Composable
private fun AppRuleRow(
    app: UsageApp,
    rule: FocusRuleEntity?,
    onToggle: (Boolean) -> Unit,
    onBudget: (Int) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Surface(color = MethodraColors.Basalt, shape = MaterialTheme.shapes.medium) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = rule != null, onCheckedChange = onToggle)
            Column(Modifier.weight(1f)) {
                Text(app.label, fontWeight = FontWeight.SemiBold)
                Text(app.packageName, color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            if (rule != null) {
                Box {
                    TextButton(onClick = { menu = true }) { Text(if (rule.dailyBudgetMinutes == 0) "No budget" else "${rule.dailyBudgetMinutes}m/day") }
                    DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                        listOf(0, 15, 30, 45, 60, 90).forEach { minutes ->
                            DropdownMenuItem(
                                text = { Text(if (minutes == 0) "No daily budget" else "$minutes min/day") },
                                onClick = { onBudget(minutes); menu = false }
                            )
                        }
                    }
                }
            }
        }
    }
}
