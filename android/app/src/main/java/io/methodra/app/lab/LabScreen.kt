package io.methodra.app.lab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.methodra.app.design.MethodraColors

@Composable
fun LabScreen(viewModel: LabViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var question by remember { mutableStateOf("") }
    var metric by remember { mutableStateOf("") }
    var metricValue by remember { mutableStateOf("") }
    var context by remember { mutableStateOf("") }
    var adhered by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().background(MethodraColors.Obsidian).padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 28.dp)
    ) {
        item {
            Text("Lab", style = MaterialTheme.typography.headlineLarge)
            Text("Run one careful personal trial. Methodra reports associations, not personal causal proof.", color = MethodraColors.Muted)
        }

        val trial = state.trial
        if (trial == null) {
            item {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Start a 14-day personal trial", style = MaterialTheme.typography.titleLarge)
                        Text("One method, one primary metric, no causal overclaiming.", color = MethodraColors.Muted)
                        OutlinedTextField(
                            value = question,
                            onValueChange = { question = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Question") },
                            placeholder = { Text("Does a protected focus block improve study consistency?") }
                        )
                        OutlinedTextField(
                            value = metric,
                            onValueChange = { metric = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Primary metric") },
                            placeholder = { Text("Problem attempts per planned day") }
                        )
                        Button(
                            onClick = { viewModel.create(question, metric) },
                            enabled = question.isNotBlank() && metric.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Start trial") }
                    }
                }
            }
        } else {
            item {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ACTIVE TRIAL", color = MethodraColors.Amber, fontWeight = FontWeight.Bold)
                        Text(trial.question, style = MaterialTheme.typography.titleLarge)
                        Text("Primary metric · ${trial.primaryMetricName}", color = MethodraColors.Muted)
                        Text("${state.summary.daysLogged} logged days · ${state.summary.adherenceRate}% adherence", color = MethodraColors.MineralBlue)
                        state.summary.averageMetric?.let { Text("Average logged metric · %.2f".format(it)) }
                        Text(state.summary.language, color = MethodraColors.Caution, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            item {
                ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Log today", style = MaterialTheme.typography.titleLarge)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = adhered, onCheckedChange = { adhered = it })
                            Text("I followed the selected method today")
                        }
                        OutlinedTextField(
                            value = metricValue,
                            onValueChange = { metricValue = it.filter { ch -> ch.isDigit() || ch == '.' || ch == '-' } },
                            label = { Text(trial.primaryMetricName) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        OutlinedTextField(context, { context = it }, label = { Text("Context note (optional)") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            viewModel.log(adhered, metricValue.toDoubleOrNull(), context)
                            metricValue = ""
                            context = ""
                        }, modifier = Modifier.fillMaxWidth()) { Text("Save today's evidence") }
                    }
                }
            }

            item {
                Text("Weekly review decision", style = MaterialTheme.typography.titleLarge)
                Text("A missed day can mean the rule, timing, opportunity, ability, or task size needs changing.", color = MethodraColors.Muted)
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("CONTINUE", "SIMPLIFY", "MODIFY").forEach { decision ->
                        OutlinedButton(onClick = { viewModel.decide(decision) }, modifier = Modifier.weight(1f)) { Text(decision.lowercase().replaceFirstChar { it.uppercase() }) }
                    }
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { viewModel.decide("STOP") }, modifier = Modifier.weight(1f)) { Text("Stop") }
                    OutlinedButton(onClick = { viewModel.decide("INCONCLUSIVE") }, modifier = Modifier.weight(1f)) { Text("Inconclusive") }
                }
            }

            item { Text("Trial log", style = MaterialTheme.typography.titleLarge) }
            items(state.entries.reversed(), key = { it.id }) { entry ->
                Surface(color = MethodraColors.Basalt, shape = MaterialTheme.shapes.medium) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(entry.localDate, fontWeight = FontWeight.SemiBold)
                            if (entry.contextNote.isNotBlank()) Text(entry.contextNote, color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
                        }
                        Text(if (entry.adhered) "Followed" else "Missed", color = if (entry.adhered) MethodraColors.Positive else MethodraColors.Caution)
                        Spacer(Modifier.width(12.dp))
                        Text(entry.metricValue?.toString() ?: "—")
                    }
                }
            }
        }
    }
}
