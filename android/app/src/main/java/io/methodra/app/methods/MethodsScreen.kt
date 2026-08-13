package io.methodra.app.methods

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.methodra.app.design.MethodraColors
import io.methodra.app.domain.BookProtocolCollection
import io.methodra.app.domain.MethodDefinition
import io.methodra.app.onboarding.EvidenceChip
import io.methodra.app.ui.MethodsViewModel

@Composable
fun MethodsScreen(viewModel: MethodsViewModel = hiltViewModel()) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    var selectedMethod by remember { mutableStateOf<MethodDefinition?>(null) }
    var selectedCollection by remember { mutableStateOf<BookProtocolCollection?>(null) }

    val filteredMethods = remember(query, viewModel.methods) {
        viewModel.methods.filter {
            query.isBlank() || it.name.contains(query, true) || it.intendedProblem.contains(query, true) ||
                it.obstacles.any { obstacle -> obstacle.label.contains(query, true) }
        }
    }
    val filteredCollections = remember(query, viewModel.collections) {
        viewModel.collections.filter {
            query.isBlank() || it.title.contains(query, true) || it.book.contains(query, true) ||
                it.author.contains(query, true) || it.summary.contains(query, true)
        }
    }

    Column(Modifier.fillMaxSize().background(MethodraColors.Obsidian).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(8.dp))
        Text("Methods", style = MaterialTheme.typography.headlineLarge)
        Text("Ten research-supported methods plus four clearly attributed practical collections.", color = MethodraColors.Muted)
        Spacer(Modifier.height(14.dp))
        OutlinedTextField(
            value = query,
            onValueChange = viewModel::setQuery,
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            placeholder = { Text("Search method, obstacle, book, or author") },
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                Text("Research-supported catalog", style = MaterialTheme.typography.titleLarge)
                Text("Evidence level and limitation are visible for every method.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            items(filteredMethods, key = { "method-${it.id}" }) { method ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt),
                    modifier = Modifier.fillMaxWidth().clickable { selectedMethod = method }
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(method.name, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            Spacer(Modifier.width(8.dp))
                            EvidenceChip(method.evidence.level)
                        }
                        Text(method.shortExplanation, color = MethodraColors.Muted)
                        Text("Addresses: ${method.obstacles.take(3).joinToString { it.label }}", style = MaterialTheme.typography.bodySmall, color = MethodraColors.MineralBlue)
                    }
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                Text("Book-inspired collections", style = MaterialTheme.typography.titleLarge)
                Text("Original Methodra protocols with attribution; a book framework is not treated as experimental proof.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
            items(filteredCollections, key = { "collection-${it.id}" }) { collection ->
                ElevatedCard(
                    colors = CardDefaults.elevatedCardColors(containerColor = MethodraColors.Basalt),
                    modifier = Modifier.fillMaxWidth().clickable { selectedCollection = collection }
                ) {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(collection.title, style = MaterialTheme.typography.titleLarge)
                                Text("Inspired by ${collection.book} · ${collection.author}", color = MethodraColors.MineralBlue, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.width(8.dp))
                            EvidenceChip(collection.evidenceLevel)
                        }
                        Text(collection.summary, color = MethodraColors.Muted)
                    }
                }
            }
        }
    }

    selectedMethod?.let { method ->
        MethodDetailDialog(method = method, onDismiss = { selectedMethod = null })
    }
    selectedCollection?.let { collection ->
        CollectionDetailDialog(collection = collection, onDismiss = { selectedCollection = null })
    }
}

@Composable
private fun MethodDetailDialog(method: MethodDefinition, onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(method.name) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EvidenceChip(method.evidence.level)
                Text(method.shortExplanation)
                Text("Intended problem", fontWeight = FontWeight.Bold)
                Text(method.intendedProblem)
                Text("May help when", fontWeight = FontWeight.Bold)
                method.mayHelpWhen.forEach { Text("• $it") }
                Text("May be unsuitable when", fontWeight = FontWeight.Bold)
                method.unsuitableWhen.forEach { Text("• $it", color = MethodraColors.Muted) }
                HorizontalDivider()
                Text("Setup questions", fontWeight = FontWeight.Bold)
                method.setupQuestions.forEach { Text("• $it") }
                Text("Protocol", fontWeight = FontWeight.Bold)
                method.steps.forEach { Text("${it.order}. ${it.title} — ${it.instruction}") }
                Text("Minimum version", fontWeight = FontWeight.Bold)
                Text(method.minimumVersion)
                Text("Evidence limitation", fontWeight = FontWeight.Bold)
                Text(method.evidence.limitation, color = MethodraColors.Caution)
                Text("Sources", fontWeight = FontWeight.Bold)
                method.sourceLabels.forEachIndexed { index, label ->
                    val url = method.sourceUrls.getOrNull(index).orEmpty()
                    if (url.isNotBlank()) {
                        TextButton(onClick = { uriHandler.openUri(url) }, contentPadding = PaddingValues(0.dp)) {
                            Text(label)
                        }
                    } else {
                        Text("• $label")
                    }
                }
                Text("Stop / adapt when", fontWeight = FontWeight.Bold)
                method.stopConditions.forEach { Text("• $it") }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun CollectionDetailDialog(collection: BookProtocolCollection, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(collection.title) },
        text = {
            Column(Modifier.heightIn(max = 520.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EvidenceChip(collection.evidenceLevel)
                Text("Inspired by ${collection.book} by ${collection.author}", color = MethodraColors.MineralBlue)
                Text(collection.summary)
                HorizontalDivider()
                Text("Protocol", fontWeight = FontWeight.Bold)
                collection.steps.forEachIndexed { index, step -> Text("${index + 1}. $step") }
                Text("Evidence note", fontWeight = FontWeight.Bold)
                Text(collection.evidenceNote, color = MethodraColors.Caution)
                Text("Methodra uses original wording and does not reproduce book passages, diagrams, or exercises.", color = MethodraColors.Muted, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}
