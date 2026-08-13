package io.methodra.app.focus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.methodra.app.MainActivity
import io.methodra.app.data.repository.FocusRepository
import io.methodra.app.design.MethodraColors
import io.methodra.app.design.MethodraTheme
import io.methodra.app.domain.UsageApp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BlockInterruptionActivity : ComponentActivity() {
    @Inject lateinit var focusRepository: FocusRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val packageName = intent.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val protectionReason = intent.getStringExtra(EXTRA_REASON).orEmpty()
        setContent {
            MethodraTheme {
                BlockedScreen(
                    protectionReason = protectionReason,
                    onReturn = { finish() },
                    onEmergencyExit = { exitReason ->
                        lifecycleScope.launch {
                            focusRepository.endSession(exitReason, emergency = true)
                            finish()
                        }
                    },
                    onDisableBudget = {
                        lifecycleScope.launch {
                            focusRepository.toggleRule(UsageApp(packageName, packageName), false)
                            finish()
                        }
                    },
                    onOpenMethodra = {
                        startActivity(Intent(this, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                        finish()
                    }
                )
            }
        }
    }

    companion object {
        const val EXTRA_PACKAGE = "package"
        const val EXTRA_REASON = "reason"
    }
}

@Composable
private fun BlockedScreen(
    protectionReason: String,
    onReturn: () -> Unit,
    onEmergencyExit: (String) -> Unit,
    onDisableBudget: () -> Unit,
    onOpenMethodra: () -> Unit
) {
    val isSession = protectionReason == "FOCUS_SESSION"
    val isSchedule = protectionReason == "FOCUS_SCHEDULE"
    var showExit by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    var coolDownSeconds by remember { mutableIntStateOf(5) }

    LaunchedEffect(showExit, isSession) {
        if (showExit && isSession) {
            coolDownSeconds = 5
            while (coolDownSeconds > 0) {
                delay(1_000)
                coolDownSeconds -= 1
            }
        }
    }

    Surface(color = MethodraColors.Obsidian, modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(28.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Text("Protection is active", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(14.dp))
            Text(
                when {
                    isSession -> "You selected this app as a distraction during the current protected block. Methodra cannot make the restriction impossible to bypass."
                    isSchedule -> "This app is inside a recurring focus window you chose. The schedule can be disabled or deleted from Methodra at any time."
                    else -> "You reached the daily usage budget you chose for this app."
                },
                color = MethodraColors.Muted
            )
            Spacer(Modifier.height(28.dp))
            Button(onClick = onReturn, modifier = Modifier.fillMaxWidth()) { Text("Go back") }
            Spacer(Modifier.height(10.dp))
            OutlinedButton(
                onClick = { if (isSchedule) onOpenMethodra() else showExit = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    when {
                        isSession -> "Emergency exit"
                        isSchedule -> "Open Methodra to edit schedule"
                        else -> "Change this rule"
                    }
                )
            }
        }
    }

    if (showExit) {
        AlertDialog(
            onDismissRequest = { showExit = false },
            title = { Text(if (isSession) "Emergency exit" else "Disable budget rule?") },
            text = {
                if (isSession) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Write a short reason. This is for your own review, not a punishment.")
                        OutlinedTextField(reason, { reason = it }, label = { Text("Reason") }, singleLine = true)
                        Text(
                            if (coolDownSeconds > 0) "Exit available in ${coolDownSeconds}s" else "You can exit now.",
                            color = MethodraColors.Muted,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                } else {
                    Text("This will remove the selected app from Methodra focus rules. You can add it again later.")
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSession || (reason.isNotBlank() && coolDownSeconds == 0),
                    onClick = { if (isSession) onEmergencyExit(reason.trim()) else onDisableBudget() }
                ) { Text("Continue") }
            },
            dismissButton = { TextButton(onClick = { showExit = false }) { Text("Cancel") } }
        )
    }
}
