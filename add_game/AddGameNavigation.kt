package com.timelimiter.android.features.add_game

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object AddGameDestination : AppDestination() {
    override val route: String = "add_game"
}

fun NavController.navigateToAddGame(navOptions: NavOptions? = null) {
    navigateWithLifecycle(AddGameDestination.route, navOptions)
}

fun NavGraphBuilder.addGameScreen(
    navigateUp: () -> Unit
) {
    composable(AddGameDestination.route) {
        AddGameScreen(
            onBack = navigateUp
        )
    }
}