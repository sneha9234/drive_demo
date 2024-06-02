package com.example.drivedemo.presentation.util

sealed class Screen(val route: String) {

    object HomeScreen : Screen(route = "home_screen")

    fun withArgs(vararg args: String?): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}