package com.timelimiter.android.features.general_aaps

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object GeneralAppsDestination : AppDestination() {
    override val route: String = "general_apps"
}

fun NavController.navigateToGeneralApps(navOptions: NavOptions? = null) {
    navigateWithLifecycle(GeneralAppsDestination.route, navOptions)
}

fun NavGraphBuilder.generalAppsScreen(
    navigateUp: () -> Unit,
    navigateToAddApp: () -> Unit
) {
    composable(
        route = GeneralAppsDestination.route
    ) {
        GeneralAppsScreen(
            onBack = navigateUp,
            onAddApp = navigateToAddApp
        )
    }
}