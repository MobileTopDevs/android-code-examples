package com.timelimiter.android.features.games

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object GamesDestination : AppDestination() {
    override val route: String = "games"
}

fun NavController.navigateToGames(navOptions: NavOptions? = null) {
    navigateWithLifecycle(GamesDestination.route, navOptions)
}

fun NavGraphBuilder.gamesScreen(
    navigateUp: () -> Unit,
    navigateToAddApp: () -> Unit
) {
    composable(
        route = GamesDestination.route
    ) {
        GamesScreen(
            onBack = navigateUp,
            onAddApp = navigateToAddApp
        )
    }
}