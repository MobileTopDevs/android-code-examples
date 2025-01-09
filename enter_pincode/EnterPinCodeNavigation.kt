package com.timelimiter.android.features.enter_pincode

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object EnterPinCodeDestination: AppDestination() {
    override val route: String = "enter_pin_code"
}

fun NavController.navigateToEnterPinCode(navOptions: NavOptions? = null) {
    navigateWithLifecycle(EnterPinCodeDestination.route, navOptions)
}

fun NavGraphBuilder.enterPinCodeScreen(
    navigateUp: () -> Unit,
    navigateToSettings: () -> Unit
) {
    composable(EnterPinCodeDestination.route) {
        EnterPinCodeScreen(
            onBack = navigateUp,
            onPinCodeEntered = navigateToSettings
        )
    }
}