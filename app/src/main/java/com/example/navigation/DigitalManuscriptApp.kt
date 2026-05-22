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
        composable("reader/{id}") { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull() ?: return@composable
            ReaderScreen(
                viewModel = viewModel,
                manuscriptId = id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
