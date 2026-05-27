package com.example.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.MainViewModel
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.PedalSettingsScreen
import com.example.ui.screens.ReaderScreen

@Composable
fun DigitalManuscriptApp(viewModel: MainViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "library") {
        composable("library") {
            LibraryScreen(
                viewModel = viewModel,
                onNavigateToReader = { id ->
                    navController.navigate("reader/$id")
                },
                onNavigateToPedalSettings = {
                    navController.navigate("pedal_settings")
                }
            )
        }
        composable("pedal_settings") {
            PedalSettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
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
                }
            )
        }
    }
}
