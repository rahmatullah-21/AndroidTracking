package com.deviceinsight.pro.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.ui.graphics.vector.ImageVector

/** All navigation destinations in the app. */
object Dest {
    const val ONBOARDING = "onboarding"
    const val DASHBOARD = "dashboard"
    const val USAGE = "usage"
    const val TIMELINE = "timeline"
    const val SECURITY = "security"
    const val MORE = "more"

    // Secondary (pushed) destinations
    const val NOTIFICATIONS = "notifications"
    const val ANALYTICS = "analytics"
    const val REPORTS = "reports"
    const val SETTINGS = "settings"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Dest.DASHBOARD, "Dashboard", Icons.Filled.Dashboard),
    BottomNavItem(Dest.USAGE, "Usage", Icons.Filled.BarChart),
    BottomNavItem(Dest.TIMELINE, "Timeline", Icons.Filled.Timeline),
    BottomNavItem(Dest.SECURITY, "Security", Icons.Filled.Security),
    BottomNavItem(Dest.MORE, "More", Icons.Filled.Apps)
)

val bottomNavRoutes = bottomNavItems.map { it.route }.toSet()
