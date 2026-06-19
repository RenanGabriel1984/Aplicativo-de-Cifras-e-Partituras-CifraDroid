package com.example.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PedalSettingsScreen
import com.example.ui.screens.ReaderScreen
import com.example.ui.screens.MaestroScreen

@Composable
fun DigitalManuscriptApp(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(
        navController = navController, 
        startDestination = "library",
        enterTransition = { fadeIn(animationSpec = tween(500)) },
        exitTransition = { fadeOut(animationSpec = tween(500)) }
    ) {
        composable("library") {
            LibraryScreen(
                viewModel = viewModel,
                onNavigateToReader = { id ->
                    navController.navigate("reader/$id")
                },
                onNavigateToPedalSettings = {
                    navController.navigate("pedal_settings")
                },
                onNavigateToMaestro = {
                    navController.navigate("maestro")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToSetlists = {
                    navController.navigate("setlists")
                }
            )
        }
        composable("pedal_settings") {
            PedalSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToMaestro = { navController.navigate("maestro") }
            )
        }
        composable("maestro") {
            MaestroScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("settings") {
            com.example.ui.screens.SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPedalSettings = { navController.navigate("pedal_settings") }
            )
        }
        composable("setlists") {
            com.example.ui.screens.RepertoiresScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEditRepertoire = { id -> navController.navigate("edit_repertoire/$id") }
            )
        }
        composable(
            "edit_repertoire/{id}",
            arguments = listOf(androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            com.example.ui.screens.EditRepertoireScreen(
                viewModel = viewModel,
                repertoireId = id,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReader = { msId, repId ->
                    navController.navigate("song_chart/$msId?repertoireId=$repId")
                }
            )
        }
        composable(
            "song_chart/{id}?repertoireId={repertoireId}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("repertoireId") { 
                    type = androidx.navigation.NavType.IntType
                    defaultValue = -1 
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            val repId = backStackEntry.arguments?.getInt("repertoireId") ?: -1
            com.example.ui.screens.SongChartScreen(
                songChartId = id,
                repertoireId = if (repId != -1) repId else null,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNavigateToSongChart = { nextId ->
                    navController.navigate("song_chart/$nextId?repertoireId=$repId") {
                        popUpTo("edit_repertoire/$repId")
                    }
                }
            )
        }
        composable(
            "reader/{id}?repertoireId={repertoireId}",
            arguments = listOf(
                androidx.navigation.navArgument("id") { type = androidx.navigation.NavType.IntType },
                androidx.navigation.navArgument("repertoireId") { 
                    type = androidx.navigation.NavType.IntType
                    defaultValue = -1 
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: return@composable
            val repId = backStackEntry.arguments?.getInt("repertoireId") ?: -1
            ReaderScreen(
                viewModel = viewModel,
                manuscriptId = id,
                repertoireId = if (repId != -1) repId else null,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToManuscript = { nextId ->
                    navController.navigate("reader/$nextId?repertoireId=$repId") {
                        popUpTo("library") // avoid giant backstacks
                    }
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                }
            )
        }
    }
}
