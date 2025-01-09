package com.timelimiter.android.features.add_general_app

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object AddGeneralAppDestination: AppDestination() {
    override val route: String = "add_general_app"
}

fun NavController.navigateToAddGeneralApp(navOptions: NavOptions? = null) {
    navigateWithLifecycle(AddGeneralAppDestination.route, navOptions)
}

fun NavGraphBuilder.addGeneralAppScreen(
    navigateUp: () -> Unit
) {
    composable(AddGeneralAppDestination.route) {
        AddGeneralAppScreen(
            onBack = navigateUp
        )
    }
}