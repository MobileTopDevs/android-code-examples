package com.timelimiter.android.features.wallpapers

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object WallpapersDestination : AppDestination() {
    override val route: String = "wallpapers"
}

fun NavController.navigateToWallpapers(navOptions: NavOptions? = null) {
    navigateWithLifecycle(WallpapersDestination.route, navOptions)
}

fun NavGraphBuilder.wallpapersScreen(
    navigateUp: () -> Unit
) {
    composable(WallpapersDestination.route) {
        WallpapersScreen(onBack = navigateUp)
    }
}