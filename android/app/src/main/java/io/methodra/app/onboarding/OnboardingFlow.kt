package io.methodra.app.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.methodra.app.design.MethodraColors
import io.methodra.app.domain.*
import io.methodra.app.ui.OnboardingViewModel

private enum class OnboardingPhase { STONE, ASSESSMENT, MATCHES }

@Composable
fun OnboardingFlow(viewModel: OnboardingViewModel = hiltViewModel()) {
    var phase by rememberSaveable { mutableStateOf(OnboardingPhase.STONE) }
    val draft by viewModel.draft.collectAsState()
    val matches by viewModel.matches.collectAsState()
    val reduceMotion by viewModel.reduceMotion.collectAsState()
    val haptics by viewModel.haptics.collectAsState()

    when (phase) {
        OnboardingPhase.STONE -> StoneOnboarding(
            reduceMotion = reduceMotion,
            hapticsEnabled = haptics,
            onComplete = { phase = OnboardingPhase.ASSESSMENT }
        )
        OnboardingPhase.ASSESSMENT -> AssessmentScreen(
            draft = draft,
            onUpdate = viewModel::update,
            onContinue = {
                viewModel.calculateMatches()
                phase = OnboardingPhase.MATCHES
            }
        )
        OnboardingPhase.MATCHES -> MatchScreen(
            matches = matches,
            onBack = { phase = OnboardingPhase.ASSESSMENT },
            onActivate = viewModel::activate
        )
    }
}

@Composable
private fun AssessmentScreen(
    draft: AssessmentDraft,
    onUpdate: ((AssessmentDraft) -> AssessmentDraft) -> Unit,
    onContinue: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MethodraColors.Obsidian)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(Modifier.height(10.dp))
        Text("One outcome. One obstacle.", style = MaterialTheme.typography.headlineLarge)
        Text("Methodra recommends at most three methods and shows why each one matched.", color = MethodraColors.Muted)

        ChoiceSection("1 · Life domain", GoalDomain.entries, draft.goalDomain, { it.label }) {
            onUpdate { d -> d.copy(goalDomain = it) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("2 · Observable outcome", fontWeight = FontWeight.SemiBold)
            OutlinedTextField(
                value = draft.desiredOutcome,
                onValueChange = { value -> onUpdate { it.copy(desiredOutcome = value) } },
                placeholder = { Text("e.g. Attempt one DSA problem on planned study days") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        ChoiceSection("3 · Main obstacle", ObstacleType.entries, draft.obstacle, { it.label }) {
            onUpdate { d -> d.copy(obstacle = it) }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("4 · Realistic time", fontWeight = FontWeight.SemiBold)
            Text("${draft.availableMinutes} minutes", color = MethodraColors.Amber)
            Slider(
                value = draft.availableMinutes.toFloat(),
                onValueChange = { value -> onUpdate { it.copy(availableMinutes = (value / 5).toInt() * 5) } },
                valueRange = 10f..120f,
                steps = 21
            )
        }

        ChoiceSection("5 · Preferred structure", StructureLevel.entries, draft.structureLevel, { it.label }) {
            onUpdate { d -> d.copy(structureLevel = it) }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = draft.highScreenTime,
                onCheckedChange = { checked -> onUpdate { it.copy(highScreenTime = checked) } }
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Digital distraction is a meaningful part of the problem")
                Text("This can increase the relevance of focus protection.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
        }

        OutlinedTextField(
            value = draft.pastFailure,
            onValueChange = { value -> onUpdate { it.copy(pastFailure = value) } },
            label = { Text("Past failed attempt (optional)") },
            modifier = Modifier.fillMaxWidth()
        )

        Text(
            "Methodra is a behavior-planning tool, not medical care. If the problem seriously affects daily functioning or safety, consider seeking qualified professional support rather than relying on a productivity protocol alone.",
            color = MethodraColors.Muted,
            style = MaterialTheme.typography.bodySmall
        )

        Button(
            onClick = onContinue,
            enabled = draft.desiredOutcome.trim().length >= 5,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Show method matches") }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun <T> ChoiceSection(
    title: String,
    options: List<T>,
    selected: T,
    label: (T) -> String,
    onSelect: (T) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(title, fontWeight = FontWeight.SemiBold)
        options.forEach { option ->
            Surface(
                color = if (option == selected) MethodraColors.ElevatedStone else MethodraColors.Basalt,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth().selectable(selected = option == selected, onClick = { onSelect(option) })
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = option == selected, onClick = null)
                    Spacer(Modifier.width(8.dp))
                    Text(label(option))
                }
            }
        }
    }
}

@Composable
private fun MatchScreen(
    matches: List<MethodMatch>,
    onBack: () -> Unit,
    onActivate: (MethodMatch, List<String>) -> Unit
) {
    var setupMatch by remember { mutableStateOf<MethodMatch?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().background(MethodraColors.Obsidian).safeDrawingPadding()
            .verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TextButton(onClick = onBack) { Text("← Change answers") }
        Text("Your strongest matches", style = MaterialTheme.typography.headlineLarge)
        Text("These are deterministic recommendations from the rules in the repository—not an AI guess.", color = MethodraColors.Muted)

        matches.forEachIndexed { index, match ->
            ElevatedCard(colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt)) {
                Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("#${index + 1}", color = MethodraColors.Amber, fontWeight = FontWeight.Bold)
                        EvidenceChip(match.method.evidence.level)
                    }
                    Text(match.method.name, style = MaterialTheme.typography.headlineMedium)
                    Text(match.method.shortExplanation, color = MethodraColors.Muted)
                    HorizontalDivider(color = MethodraColors.ElevatedStone)
                    Text("Why it matched", fontWeight = FontWeight.SemiBold)
                    match.reasons.take(3).forEach { Text("• $it") }
                    Text("Minimum version", color = MethodraColors.Muted, fontWeight = FontWeight.SemiBold)
                    Text(match.method.minimumVersion)
                    Button(onClick = { setupMatch = match }, modifier = Modifier.fillMaxWidth()) { Text("Set up this protocol") }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }

    setupMatch?.let { match ->
        ProtocolSetupDialog(
            match = match,
            onDismiss = { setupMatch = null },
            onStart = { answers -> onActivate(match, answers) }
        )
    }
}

@Composable
private fun ProtocolSetupDialog(
    match: MethodMatch,
    onDismiss: () -> Unit,
    onStart: (List<String>) -> Unit
) {
    val questions = match.method.setupQuestions
    val answers = remember(match.method.id) { mutableStateListOf(*Array(questions.size) { "" }) }
    val ready = questions.isNotEmpty() && answers.all { it.trim().length >= 2 }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Make ${match.method.name} concrete") },
        text = {
            Column(
                modifier = Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Answer the setup questions before activation. These answers become part of today's protocol.", color = MethodraColors.Muted)
                questions.forEachIndexed { index, question ->
                    OutlinedTextField(
                        value = answers[index],
                        onValueChange = { answers[index] = it },
                        label = { Text(question) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }
        },
        confirmButton = {
            TextButton(enabled = ready, onClick = { onStart(answers.toList()) }) { Text("Start protocol") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun EvidenceChip(level: EvidenceLevel) {
    val text = "Level ${level.name} · ${level.label}"
    Surface(
        color = MethodraColors.ElevatedStone,
        shape = MaterialTheme.shapes.large
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MethodraColors.Bone
        )
    }
}
