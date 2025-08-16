package com.example.clientemovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clientemovil.ui.screens.author.AuthorsFormScreen
import com.example.clientemovil.ui.screens.catalogos.AuthorsScreen
import com.example.clientemovil.ui.screens.catalogos.GenresScreen
import com.example.clientemovil.ui.screens.catalogos.PublishersScreen
import com.example.clientemovil.ui.screens.genre.GenresFormScreen
import com.example.clientemovil.ui.screens.publisher.PublishersFormScreen
import com.example.clientemovil.ui.screens.role.RoleFormScreen
import com.example.clientemovil.ui.screens.role.RolesScreen
import com.example.clientemovil.ui.screens.sales.SalesFormScreen
import com.example.clientemovil.ui.screens.sales.SalesScreen
import com.example.clientemovil.ui.screens.user.UserFormScreen
import com.example.clientemovil.ui.screens.user.UsersScreen
import com.example.clientemovil.ui.theme.ClienteMovilTheme

/**
 * Clase sellada para definir todas las rutas de navegación de la aplicación.
 */
sealed class Screen(val route: String, val label: String? = null, val icon: ImageVector? = null) {
    // Pantallas principales con ícono para la barra de navegación
    object Users : Screen("users", "Usuarios", Icons.Default.Person)
    object Roles : Screen("roles", "Roles", Icons.Default.List)
    object Sales : Screen("sales", "Ventas", Icons.Default.ShoppingCart)
    object Authors : Screen("authors", "Autores", Icons.Default.Person)
    object Genres : Screen("genres", "Géneros", Icons.Default.List)
    object Publishers : Screen("publishers", "Editoriales", Icons.Default.List)

    // Pantallas de formulario sin ícono en la barra de navegación
    object UserForm : Screen("user_form/{userId}", "UserForm")
    object RoleForm : Screen("role_form/{roleId}", "RoleForm")
    object SaleForm : Screen("sale_form/{saleId}", "SaleForm")
    object AuthorsForm : Screen("authors_form/{authorId}", "AutorForm")
    object GenresForm : Screen("genres_form/{genreId}", "GéneroForm")
    object PublishersForm : Screen("publishers_form/{publisherId}", "EditorialForm")
}

// Lista de elementos para la barra de navegación inferior
val bottomNavItems = listOf(
    Screen.Authors,
    Screen.Genres,
    Screen.Publishers,
    Screen.Users,
    Screen.Roles,
    Screen.Sales
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClienteMovilTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MainScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon?.let { Icon(it, contentDescription = null) } },
                        label = { screen.label?.let { Text(it) } },
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
            startDestination = Screen.Authors.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Rutas de catálogos
            composable(Screen.Authors.route) { AuthorsScreen(navController) }
            composable(Screen.Genres.route) { GenresScreen(navController) }
            composable(Screen.Publishers.route) { PublishersScreen(navController) }

            // Rutas de usuarios, roles y ventas
            composable(Screen.Users.route) { UsersScreen(navController) }
            composable(Screen.Roles.route) { RolesScreen(navController) }
            composable(Screen.Sales.route) { SalesScreen(navController) }

            // Rutas de formularios
            composable(Screen.AuthorsForm.route) { backStackEntry ->
                val authorId = backStackEntry.arguments?.getString("authorId")?.toIntOrNull()
                AuthorsFormScreen(authorId = authorId, onBack = { navController.popBackStack() })
            }
            composable(Screen.GenresForm.route) { backStackEntry ->
                val genreId = backStackEntry.arguments?.getString("genreId")?.toIntOrNull()
                GenresFormScreen(genreId = genreId, onBack = { navController.popBackStack() })
            }
            composable(Screen.PublishersForm.route) { backStackEntry ->
                val publisherId = backStackEntry.arguments?.getString("publisherId")?.toIntOrNull()
                PublishersFormScreen(publisherId = publisherId, onBack = { navController.popBackStack() })
            }
            composable(Screen.UserForm.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                UserFormScreen(userId = userId, onBack = { navController.popBackStack() })
            }
            composable(Screen.RoleForm.route) { backStackEntry ->
                val roleId = backStackEntry.arguments?.getString("roleId")
                RoleFormScreen(roleId = roleId, onBack = { navController.popBackStack() })
            }
            composable(Screen.SaleForm.route) { backStackEntry ->
                val saleId = backStackEntry.arguments?.getString("saleId")
                SalesFormScreen(saleId = saleId, onBack = { navController.popBackStack() })
            }
        }
    }
}
