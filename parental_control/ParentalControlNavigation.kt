package com.timelimiter.android.features.parental_control

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object ParentalControlDestination : AppDestination() {
    override val route: String = "parental_control"
}

fun NavController.navigateToParentalControl(navOptions: NavOptions? = null) {
    navigateWithLifecycle(ParentalControlDestination.route, navOptions)
}

fun NavGraphBuilder.parentalControlScreen(
    navigateUp: () -> Unit,
    navigateToTopApps: () -> Unit,
    navigateToGeneralApps: () -> Unit,
    navigateToGames: () -> Unit
) {
    composable(
        route = ParentalControlDestination.route
    ) {
        ParentalControlScreen(
            onBack = navigateUp,
            onTopApps = navigateToTopApps,
            onGeneralApps = navigateToGeneralApps,
            onGames = navigateToGames
        )
    }
}