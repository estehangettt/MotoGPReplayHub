package com.estehangettt.motogp.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.estehangettt.motogp.data.EventResponse
import com.estehangettt.motogp.ui.DetailScreen
import com.estehangettt.motogp.ui.HomeScreen
import com.estehangettt.motogp.ui.PlayerScreen
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home",
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(300)) }
    ) {
        composable("home") {
            HomeScreen(onEventClick = { event, round ->
                navController.currentBackStackEntry?.savedStateHandle?.set("event", event)
                navController.currentBackStackEntry?.savedStateHandle?.set("round", round)
                navController.navigate("detail")
            })
        }
        composable("detail") {
            val event = navController.previousBackStackEntry?.savedStateHandle?.get<EventResponse>("event")
            val round = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("round") ?: 1
            
            if (event != null) {
                DetailScreen(
                    event = event,
                    round = round,
                    onBackClick = { navController.popBackStack() },
                    onSessionClick = { embedUrl, title ->
                        val encodedUrl = URLEncoder.encode(embedUrl, "UTF-8")
                        val encodedTitle = URLEncoder.encode(title, "UTF-8")
                        navController.navigate("player/$encodedUrl/$encodedTitle")
                    }
                )
            }
        }
        composable(
            route = "player/{embedUrl}/{title}",
            arguments = listOf(
                navArgument("embedUrl") { type = NavType.StringType },
                navArgument("title") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("embedUrl") ?: ""
            val encodedTitle = backStackEntry.arguments?.getString("title") ?: ""
            val decodedUrl = URLDecoder.decode(encodedUrl, "UTF-8")
            val decodedTitle = URLDecoder.decode(encodedTitle, "UTF-8")
            PlayerScreen(embedUrl = decodedUrl, title = decodedTitle, onBackClick = { navController.popBackStack() })
        }
    }
}
