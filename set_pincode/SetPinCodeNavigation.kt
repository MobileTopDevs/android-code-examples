package com.timelimiter.android.features.set_pincode

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.timelimiter.android.common_ui.AppDestination
import com.timelimiter.android.common_ui.utils.navigateWithLifecycle

object SetPinCodeDestination: AppDestination() {
    override val route: String = "set_pin_code"
}

fun NavController.navigateToSetPinCode(navOptions: NavOptions? = null) {
    navigateWithLifecycle(SetPinCodeDestination.route, navOptions)
}

fun NavGraphBuilder.setPinCodeScreen() {
    composable(SetPinCodeDestination.route) {
        SetPinCodeScreen()
    }
}