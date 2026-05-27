package com.deviceinsight.pro.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.deviceinsight.pro.presentation.analytics.AnalyticsScreen
import com.deviceinsight.pro.presentation.cloud.CloudSyncScreen
import com.deviceinsight.pro.presentation.dashboard.DashboardScreen
import com.deviceinsight.pro.presentation.messages.MessagesScreen
import com.deviceinsight.pro.presentation.more.MoreScreen
import com.deviceinsight.pro.presentation.notifications.NotificationsScreen
import com.deviceinsight.pro.presentation.onboarding.PermissionsScreen
import com.deviceinsight.pro.presentation.reports.ReportsScreen
import com.deviceinsight.pro.presentation.security.SecurityScreen
import com.deviceinsight.pro.presentation.settings.SettingsScreen
import com.deviceinsight.pro.presentation.timeline.TimelineScreen
import com.deviceinsight.pro.presentation.usage.AppUsageScreen
import com.deviceinsight.pro.utils.PermissionUtils

@Composable
fun DeviceInsightRoot() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val startRoute = remember {
        val ready = PermissionUtils.hasUsageAccess(context) &&
            PermissionUtils.hasNotificationAccess(context)
        if (ready) Dest.DASHBOARD else Dest.ONBOARDING
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) AppBottomBar(navController, currentRoute)
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startRoute,
            modifier = Modifier.padding(padding)
        ) {
            composable(Dest.ONBOARDING) {
                PermissionsScreen(
                    onContinue = {
                        navController.navigate(Dest.DASHBOARD) {
                            popUpTo(Dest.ONBOARDING) { inclusive = true }
                        }
                    }
                )
            }
            composable(Dest.DASHBOARD) {
                DashboardScreen(onNavigate = { route -> navController.navigate(route) })
            }
            composable(Dest.USAGE) { AppUsageScreen() }
            composable(Dest.TIMELINE) { TimelineScreen() }
            composable(Dest.SECURITY) { SecurityScreen() }
            composable(Dest.MORE) {
                MoreScreen(onNavigate = { route -> navController.navigate(route) })
            }

            composable(Dest.MESSAGES) { MessagesScreen(onBack = { navController.popBackStack() }) }
            composable(Dest.NOTIFICATIONS) { NotificationsScreen(onBack = { navController.popBackStack() }) }
            composable(Dest.ANALYTICS) { AnalyticsScreen(onBack = { navController.popBackStack() }) }
            composable(Dest.REPORTS) { ReportsScreen(onBack = { navController.popBackStack() }) }
            composable(Dest.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCloudSync = { navController.navigate(Dest.CLOUD_SYNC) }
                )
            }
            composable(Dest.CLOUD_SYNC) { CloudSyncScreen(onBack = { navController.popBackStack() }) }
        }
    }
}

@Composable
private fun AppBottomBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
