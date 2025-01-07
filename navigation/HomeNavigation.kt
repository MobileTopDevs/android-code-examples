package com.zippe.client.features.home.navigation

import androidx.navigation.NavGraphBuilder
import com.zippe.client.common_ui.AppDestination
import com.zippe.client.common_ui.components.composable
import com.zippe.client.features.home.HomeScreen

object HomeDestination : AppDestination() {
    override val route = "home"
}

fun NavGraphBuilder.homeScreen(
    openDrawer: () -> Unit
) {
    composable(
        route = HomeDestination.route,
    ) {
        HomeScreen(
            openDrawer = openDrawer
        )
    }
}
