// app/src/main/java/com/example/securevault/ui/navigation/Navigation.kt
package com.securevault.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.securevault.ui.screens.detail.DetailScreen
import com.securevault.ui.screens.form.FormScreen
import com.securevault.ui.screens.main.MainScreen
import com.securevault.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Main.route) {
        // Main screen - password list
        composable(Screen.Main.route) {
            MainScreen(navController = navController)
        }

        // Detail screen - password details
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("passwordId") { type = NavType.StringType })
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId") ?: ""
            DetailScreen(
                passwordId = passwordId,
                navController = navController
            )
        }

        // Form screen - add/edit password
        composable(
            route = Screen.Form.route,
            arguments = listOf(
                navArgument("passwordId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val passwordId = backStackEntry.arguments?.getString("passwordId")
            FormScreen(
                passwordId = passwordId,
                navController = navController
            )
        }

        // Settings screen
        composable(Screen.Settings.route) {
            SettingsScreen(navController = navController)
        }
    }
}