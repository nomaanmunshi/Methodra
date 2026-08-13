package io.methodra.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.methodra.app.design.MethodraColors
import io.methodra.app.focus.FocusScreen
import io.methodra.app.lab.LabScreen
import io.methodra.app.methods.MethodsScreen
import io.methodra.app.onboarding.OnboardingFlow
import io.methodra.app.settings.SettingsScreen
import io.methodra.app.today.TodayScreen

private data class PrimaryDestination(val route: String, val label: String, val icon: ImageVector)
private val primary = listOf(
    PrimaryDestination("today", "Today", Icons.Default.Today),
    PrimaryDestination("methods", "Methods", Icons.Default.MenuBook),
    PrimaryDestination("focus", "Focus", Icons.Default.CenterFocusStrong),
    PrimaryDestination("lab", "Lab", Icons.Default.Science)
)

@Composable
fun MethodraApp(viewModel: AppViewModel = hiltViewModel()) {
    val onboardingComplete by viewModel.onboardingComplete.collectAsStateWithLifecycle()

    when (onboardingComplete) {
        null -> Box(Modifier.fillMaxSize().background(MethodraColors.Obsidian), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MethodraColors.Amber)
        }
        false -> OnboardingFlow()
        true -> MainShell()
    }
}

@Composable
private fun MainShell() {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val isPrimary = primary.any { it.route == route }

    Scaffold(
        containerColor = MethodraColors.Obsidian,
        topBar = {
            if (isPrimary) {
                TopAppBar(
                    title = { Text("METHODRA", color = MethodraColors.Muted, style = MaterialTheme.typography.labelLarge) },
                    actions = {
                        IconButton(onClick = { nav.navigate("settings") }) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MethodraColors.Obsidian)
                )
            }
        },
        bottomBar = {
            if (isPrimary) {
                NavigationBar(containerColor = MethodraColors.Basalt) {
                    primary.forEach { destination ->
                        NavigationBarItem(
                            selected = route == destination.route,
                            onClick = {
                                nav.navigate(destination.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MethodraColors.Amber,
                                selectedTextColor = MethodraColors.Bone,
                                indicatorColor = MethodraColors.ElevatedStone,
                                unselectedIconColor = MethodraColors.Muted,
                                unselectedTextColor = MethodraColors.Muted
                            )
                        )
                    }
                }
            }
        }
    ) { padding ->
        NavHost(navController = nav, startDestination = "today", modifier = Modifier.fillMaxSize().padding(padding), route = "main") {
            composable("today") { Box(Modifier.fillMaxSize().background(MethodraColors.Obsidian)) { TodayScreen(onOpenFocus = { nav.navigate("focus") }) } }
            composable("methods") { MethodsScreen() }
            composable("focus") { FocusScreen() }
            composable("lab") { LabScreen() }
            composable("settings") { SettingsScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
