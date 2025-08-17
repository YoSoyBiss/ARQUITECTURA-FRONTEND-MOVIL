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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogosMainScreen() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                catalogItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = CatalogScreen.Authors.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(CatalogScreen.Authors.route) { AuthorsScreen(navController) }
            composable(CatalogScreen.Genres.route) { GenresScreen(navController) }
            composable(CatalogScreen.Publishers.route) { PublishersScreen(navController) }
            // Nuevas rutas para Productos
            composable(CatalogScreen.Products.route) { ProductsScreen(navController) }

            composable(CatalogScreen.AuthorsForm.route) { backStackEntry ->
                val authorId = backStackEntry.arguments?.getString("authorId")?.toIntOrNull()
                AuthorsFormScreen(authorId = authorId, onBack = { navController.popBackStack() })
            }
            composable(CatalogScreen.GenresForm.route) { backStackEntry ->
                val genreId = backStackEntry.arguments?.getString("genreId")?.toIntOrNull()
                GenresFormScreen(genreId = genreId, onBack = { navController.popBackStack() })
            }
            composable(CatalogScreen.PublishersForm.route) { backStackEntry ->
                val publisherId = backStackEntry.arguments?.getString("publisherId")?.toIntOrNull()
                PublishersFormScreen(publisherId = publisherId, onBack = { navController.popBackStack() })
            }
            // Nuevas rutas para el formulario de Productos
            composable(CatalogScreen.ProductsForm.route) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
                ProductsFormScreen(productId = productId, onBack = { navController.popBackStack() })
            }
        }
    }
}