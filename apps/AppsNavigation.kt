package com.timelimiter.android.features.apps

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object AppsDestination : AppDestination() {
    override val route: String = "apps"
}

fun NavController.navigateToApps(navOptions: NavOptions? = null) {
    navigateWithLifecycle(AppsDestination.route, navOptions)
}

fun NavGraphBuilder.appsScreen(
    navigateToWallpapers: () -> Unit,
    navigateToSettings: () -> Unit
) {
    composable(route = AppsDestination.route) {
        AppsScreen(
            navigateToWallpapers = navigateToWallpapers,
            navigateToSettings = navigateToSettings
        )
    }
}
