package com.example.whatstheplan.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.whatstheplan.AppContainer
import com.example.whatstheplan.MainActivity
import com.example.whatstheplan.domain.model.UserSettings
import com.example.whatstheplan.notifications.CheckInScheduler
import com.example.whatstheplan.ui.navigation.Routes
import com.example.whatstheplan.ui.screens.CheckInScreen
import com.example.whatstheplan.ui.screens.HistoryScreen
import com.example.whatstheplan.ui.screens.InsightsScreen
import com.example.whatstheplan.ui.screens.MorningScreen
import com.example.whatstheplan.ui.screens.PrivacyScreen
import com.example.whatstheplan.ui.screens.ReflectionScreen
import com.example.whatstheplan.ui.screens.SettingsScreen
import com.example.whatstheplan.ui.screens.SetupScreen
import com.example.whatstheplan.ui.screens.TodayScreen
import com.example.whatstheplan.ui.theme.WhatsThePlanTheme
import kotlinx.coroutines.flow.StateFlow

@Composable
fun WhatsThePlanApp(
    container: AppContainer,
    notificationDestinationFlow: StateFlow<String?>,
    consumeNotificationDestination: () -> Unit,
) {
    val settings by container.settingsRepository.settingsFlow.collectAsStateWithLifecycle(UserSettings())
    val todayPlan by container.dailyPlanRepository.observeToday().collectAsStateWithLifecycle(null)
    val notificationDestination by notificationDestinationFlow.collectAsStateWithLifecycle(null)

    WhatsThePlanTheme(themeMode = settings.themeMode) {
        val navController = rememberNavController()
        val context = LocalContext.current
        val backStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = backStackEntry?.destination?.route
        val mainRoutes = setOf(Routes.TODAY, Routes.HISTORY, Routes.INSIGHTS, Routes.SETTINGS)

        LaunchedEffect(
            settings.setupComplete,
            settings.checkInsEnabled,
            settings.checkInIntervalMinutes,
            settings.activeStartMinutes,
            settings.activeEndMinutes,
            settings.notificationSound,
        ) {
            if (settings.setupComplete) {
                CheckInScheduler.schedule(context, settings)
            } else {
                CheckInScheduler.cancel(context)
            }
        }

        LaunchedEffect(settings.setupComplete, todayPlan?.id, currentRoute) {
            when {
                !settings.setupComplete && currentRoute != Routes.SETUP -> {
                    navController.navigate(Routes.SETUP) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
                settings.setupComplete && todayPlan == null && currentRoute != Routes.MORNING -> {
                    navController.navigate(Routes.MORNING) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
                settings.setupComplete && todayPlan != null && currentRoute == Routes.SETUP -> {
                    navController.navigate(Routes.TODAY) {
                        popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                    }
                }
            }
        }

        LaunchedEffect(notificationDestination, settings.setupComplete) {
            val destination = notificationDestination ?: return@LaunchedEffect
            if (settings.setupComplete) {
                when (destination) {
                    MainActivity.DESTINATION_CHECK_IN -> {
                        navController.navigate(Routes.CHECK_IN) { launchSingleTop = true }
                    }
                    MainActivity.DESTINATION_MORNING -> {
                        navController.navigate(Routes.MORNING) { launchSingleTop = true }
                    }
                    MainActivity.DESTINATION_REFLECTION -> {
                        navController.navigate(Routes.REFLECTION) { launchSingleTop = true }
                    }
                }
                consumeNotificationDestination()
            }
        }

        Scaffold(
            bottomBar = {
                if (settings.setupComplete && currentRoute in mainRoutes) {
                    BottomNavigationBar(
                        currentRoute = currentRoute,
                        onRouteSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                    )
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Routes.SETUP,
                modifier = Modifier.padding(innerPadding),
            ) {
                composable(Routes.SETUP) {
                    SetupScreen(
                        settingsRepository = container.settingsRepository,
                        usageStatsReader = container.usageStatsReader,
                    )
                }
                composable(Routes.MORNING) {
                    MorningScreen(
                        planRepository = container.dailyPlanRepository,
                        funFactRepository = container.funFactRepository,
                        onDone = {
                            navController.navigate(Routes.TODAY) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    inclusive = true
                                }
                            }
                        },
                    )
                }
                composable(Routes.TODAY) {
                    TodayScreen(
                        container = container,
                        onNavigateMorning = { navController.navigate(Routes.MORNING) },
                        onNavigateCheckIn = { navController.navigate(Routes.CHECK_IN) },
                        onNavigateReflection = { navController.navigate(Routes.REFLECTION) },
                    )
                }
                composable(Routes.CHECK_IN) {
                    CheckInScreen(
                        checkInRepository = container.checkInRepository,
                        funFactRepository = container.funFactRepository,
                        onDone = {
                            navController.navigate(Routes.TODAY) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(Routes.REFLECTION) {
                    ReflectionScreen(
                        reflectionRepository = container.dailyReflectionRepository,
                        onDone = { navController.navigate(Routes.TODAY) { launchSingleTop = true } },
                    )
                }
                composable(Routes.HISTORY) {
                    HistoryScreen(
                        container = container,
                    )
                }
                composable(Routes.INSIGHTS) {
                    InsightsScreen(
                        container = container,
                    )
                }
                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        container = container,
                        onPrivacy = { navController.navigate(Routes.PRIVACY) },
                    )
                }
                composable(Routes.PRIVACY) {
                    PrivacyScreen()
                }
            }
        }
    }
}

@Composable
private fun BottomNavigationBar(
    currentRoute: String?,
    onRouteSelected: (String) -> Unit,
) {
    val items = listOf(
        NavItem(Routes.TODAY, "Home", Icons.Default.Home),
        NavItem(Routes.HISTORY, "History", Icons.Default.History),
        NavItem(Routes.INSIGHTS, "Insights", Icons.Default.BarChart),
        NavItem(Routes.SETTINGS, "Settings", Icons.Default.Settings),
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onRouteSelected(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

private data class NavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
