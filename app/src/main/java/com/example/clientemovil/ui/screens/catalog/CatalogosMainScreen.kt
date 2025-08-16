package com.example.clientemovil.ui.screens.catalogos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clientemovil.CatalogScreen
import com.example.clientemovil.catalogItems
import com.example.clientemovil.ui.screens.author.AuthorsFormScreen
import com.example.clientemovil.ui.screens.genre.GenresFormScreen
import com.example.clientemovil.ui.screens.publisher.PublishersFormScreen


@Composable
fun CatalogosMainScreen() {
    val navController = rememberNavController()

    // Este es el NavHost interno para las pantallas de catálogos
    NavHost(
        navController = navController,
        startDestination = CatalogScreen.Authors.route
    ) {
        composable(CatalogScreen.Authors.route) { AuthorsScreen(navController) }
        composable(CatalogScreen.Genres.route) { GenresScreen(navController) }
        composable(CatalogScreen.Publishers.route) { PublishersScreen(navController) }
        composable(
            route = CatalogScreen.AuthorsForm.route,
            arguments = emptyList()
        ) { backStackEntry ->
            val authorId = backStackEntry.arguments?.getString("authorId")?.toIntOrNull()
            AuthorsFormScreen(authorId = authorId, onBack = { navController.popBackStack() })
        }
        composable(
            route = CatalogScreen.GenresForm.route,
            arguments = emptyList()
        ) { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId")?.toIntOrNull()
            GenresFormScreen(genreId = genreId, onBack = { navController.popBackStack() })
        }
        composable(
            route = CatalogScreen.PublishersForm.route,
            arguments = emptyList()
        ) { backStackEntry ->
            val publisherId = backStackEntry.arguments?.getString("publisherId")?.toIntOrNull()
            PublishersFormScreen(publisherId = publisherId, onBack = { navController.popBackStack() })
        }
    }
}
