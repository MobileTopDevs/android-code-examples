package com.zippe.client.features.auth.password.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.zippe.client.common_ui.AppDestination
import com.zippe.client.common_ui.components.composable
import com.zippe.client.common_ui.utils.navigateWithLifecycle
import com.zippe.client.features.auth.password.EnterPasswordScreen

object EnterPasswordDestination : AppDestination() {
    override val route: String = "enter_password"

    const val nameArg = "name"
    const val phoneArg = "phone"
    const val verificationTokenArg = "verificationToken"

    val routeWithArgs = "$route/{$nameArg}/{$phoneArg}/{$verificationTokenArg}"
    val arguments = listOf(
        navArgument(nameArg) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(phoneArg) {
            type = NavType.StringType
            nullable = true
        },
        navArgument(verificationTokenArg) {
            type = NavType.StringType
            nullable = true
        }
    )
}

fun NavController.navigateToEnterPassword(
    name: String? = null,
    phone: String? = null,
    verificationToken: String? = null,
    navOptions: NavOptions? = null
) {
    navigateWithLifecycle(
        route = "${EnterPasswordDestination.route}/$name/$phone/$verificationToken",
        navOptions = navOptions
    )
}

fun NavGraphBuilder.enterPasswordScreen(
    navigateToVerificationScreen: (phone: String, password: String) -> Unit,
    navigateUp: () -> Unit
) {
    composable(
        route = EnterPasswordDestination.routeWithArgs,
        arguments = EnterPasswordDestination.arguments
    ) {
        EnterPasswordScreen(
            navigateToVerificationScreen = navigateToVerificationScreen,
            navigateUp = navigateUp
        )
    }
}

