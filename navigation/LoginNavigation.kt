package com.phoenixflex.android.features.login.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import com.phoenixflex.android.common_ui.components.composable
import com.phoenixflex.android.common_ui.AppDestination
import com.phoenixflex.android.common_ui.utils.navigateWithLifecycle
import com.phoenixflex.android.features.login.LoginScreen

object Login : AppDestination() {
    override val route = "login"
}

fun NavController.navigateToLogin(navOptions: NavOptions) {
    navigateWithLifecycle(
        route = Login.route,
        navOptions = navOptions
    )
}

fun NavGraphBuilder.loginScreen(
    navigateToForgotPassword: () -> Unit,
    navigateToSignUp: () -> Unit
) {
    composable(route = Login.route) {
        LoginScreen(
            navigateToForgotPassword = navigateToForgotPassword,
            navigateToSignUp = navigateToSignUp
        )
    }
}
