// app/src/main/java/com/example/securevault/ui/navigation/Screen.kt
package com.securevault.ui.navigation

sealed class Screen(val route: String) {
    object Main : Screen("main")
    object Detail : Screen("detail/{passwordId}") {
        fun createRoute(passwordId: String) = "detail/$passwordId"
    }
    object Form : Screen("form?passwordId={passwordId}") {
        fun createRoute(passwordId: String? = null) = passwordId?.let { "form?passwordId=$it" } ?: "form"
    }
    object Settings : Screen("settings")
}