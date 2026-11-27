package com.estehangettt.motogp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.ui.DetailScreen
import com.estehangettt.motogp.ui.HomeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300))
        }
    ) {
        composable("home") {
            HomeScreen(onEventClick = { event ->
                navController.currentBackStackEntry?.savedStateHandle?.set("event", event)
                navController.navigate("detail")
            })
        }
        composable("detail") {
            val event = navController.previousBackStackEntry?.savedStateHandle?.get<EventResponse>("event")
            if (event != null) {
                DetailScreen(event = event, onBackClick = { navController.popBackStack() })
            }
        }
    }
}
