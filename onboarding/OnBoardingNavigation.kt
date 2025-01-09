package com.timelimiter.android.features.onboarding

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object OnBoardingDestination: AppDestination() {
    override val route: String = "on_boarding"
}

fun NavController.navigateToOnBoarding(navOptions: NavOptions? = null) {
    navigateWithLifecycle(OnBoardingDestination.route, navOptions)
}

fun NavGraphBuilder.onBoardingScreen() {
    composable(route = OnBoardingDestination.route) {
        OnBoardingScreen()
    }
}