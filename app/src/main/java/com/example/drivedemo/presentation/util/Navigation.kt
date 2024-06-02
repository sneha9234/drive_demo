package com.example.drivedemo.presentation.util

import android.annotation.SuppressLint
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.drivedemo.presentation.home.HomeScreen

@Composable
fun NavigationGraph(
    navController: NavHostController,
    startDestination: String,
    loggedInStatus: Boolean,
    scrollState: LazyListState
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(
            route = Screen.HomeScreen.route
        ) {
            HomeScreen(
                scrollState = scrollState, loggedInStatus = loggedInStatus
            )
        }
    }

}

@SuppressLint("UnusedMaterialScaffoldPaddingParameter")
@Composable
fun SetupNavigation(startDestination: String, loggedInStatus: Boolean) {

    val navController = rememberNavController()


    val scrollState = rememberLazyListState()

    NavigationGraph(
        navController = navController,
        scrollState = scrollState,
        startDestination = startDestination,
        loggedInStatus = loggedInStatus
    )

}
