package com.timelimiter.android.features.top_apps

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object TopAppsDestination: AppDestination() {
    override val route: String = "info"
}

fun NavController.navigateToTopApps(navOptions: NavOptions? = null) {
    navigateWithLifecycle(TopAppsDestination.route, navOptions)
}

fun NavGraphBuilder.topsAppsScreen(
    navigateUp: () -> Unit
) {
    composable(TopAppsDestination.route) {
        TopAppsScreen(
            onBack = navigateUp
        )
    }
}