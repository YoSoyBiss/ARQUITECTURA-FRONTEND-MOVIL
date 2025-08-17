package com.example.clientemovil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clientemovil.ui.screens.more.AboutScreen
import com.example.clientemovil.ui.screens.login.RegisterScreen
import com.example.clientemovil.ui.screens.login.LoginScreen
import com.example.clientemovil.ui.screens.author.AuthorsFormScreen
import com.example.clientemovil.ui.screens.catalogos.AuthorsScreen
import com.example.clientemovil.ui.screens.catalogos.GenresScreen
import com.example.clientemovil.ui.screens.catalogos.ProductsFormScreen
import com.example.clientemovil.ui.screens.catalogos.ProductsScreen
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
 * Clase que define una ruta de navegación con sus propiedades.
 * Usamos un data class para facilitar la creación de instancias.
 */
data class Screen(val route: String, val label: String, val icon: ImageVector? = null)

// Menús de la barra inferior según el rol
val adminNavItems = listOf(
    Screen("authors", "Autores", Icons.Default.Person),
    Screen("genres", "Géneros", Icons.Default.List),
    Screen("publishers", "Editoriales", Icons.Default.Business),
    Screen("products", "Productos", Icons.Default.Book),
    Screen("users", "Usuarios", Icons.Default.Person),
    Screen("roles", "Roles", Icons.Default.List),
    Screen("sales", "Ventas", Icons.Default.ShoppingCart),
)

val consultantNavItems = listOf(
    Screen("products", "Productos", Icons.Default.Book),
    Screen("about", "Acerca de", Icons.Default.Info),
)

val sellerNavItems = listOf(
    Screen("products", "Productos", Icons.Default.Book),
    Screen("sales", "Ventas", Icons.Default.ShoppingCart),
    Screen("about", "Acerca de", Icons.Default.Info),
)

val otherNavItems = listOf(
    Screen("about", "Acerca de", Icons.Default.Info),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClienteMovilTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Función principal que maneja el NavHost raíz de la aplicación.
 * Este NavHost maneja la navegación de alto nivel (login, registro, pantalla principal).
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        // 🎯 RUTAS DE LOGIN Y REGISTRO
        composable("login") {
            LoginScreen(
                // Al iniciar sesión con éxito, navegamos a una ruta de "main" y pasamos el rol como argumento.
                onLoginSuccess = { role ->
                    navController.navigate("main_screen/${role}") {
                        // Limpiamos el back stack para que el usuario no pueda regresar a la pantalla de login.
                        popUpTo(navController.graph.id) {
                            inclusive = true
                        }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.popBackStack()
                },
                onLoginClick = { navController.popBackStack() }
            )
        }

        // 🎯 RUTA PRINCIPAL CON LA BARRA INFERIOR
        // Esta ruta recibe el rol del usuario para mostrar el menú correcto.
        composable("main_screen/{userRole}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("userRole")
            if (userRole != null) {
                // Llamamos a la pantalla principal, que contendrá su propio NavHost.
                MainScreenWithBottomBar(
                    navController = navController, // Se pasa el navController principal para la navegación de formularios
                    userRole = userRole
                )
            }
        }

        // 🎯 RUTAS DE FORMULARIO (navegación que no tiene barra inferior)
        // Estas rutas se definen en el NavHost principal para que puedan ser accesibles desde
        // cualquier pantalla, incluyendo las de la barra inferior.
        composable("authors_form/{authorId}") { backStackEntry ->
            val authorId = backStackEntry.arguments?.getString("authorId")?.toIntOrNull()
            AuthorsFormScreen(authorId = authorId, onBack = { navController.popBackStack() })
        }
        composable("genres_form/{genreId}") { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId")?.toIntOrNull()
            GenresFormScreen(genreId = genreId, onBack = { navController.popBackStack() })
        }
        composable("publishers_form/{publisherId}") { backStackEntry ->
            val publisherId = backStackEntry.arguments?.getString("publisherId")?.toIntOrNull()
            PublishersFormScreen(publisherId = publisherId, onBack = { navController.popBackStack() })
        }
        composable("products_form/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
            ProductsFormScreen(productId = productId, onBack = { navController.popBackStack() })
        }
        composable("user_form/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserFormScreen(userId = userId, onBack = { navController.popBackStack() })
        }
        composable("role_form/{roleId}") { backStackEntry ->
            val roleId = backStackEntry.arguments?.getString("roleId")
            RoleFormScreen(roleId = roleId, onBack = { navController.popBackStack() })
        }
        composable("sale_form/{saleId}") { backStackEntry ->
            val saleId = backStackEntry.arguments?.getString("saleId")
            SalesFormScreen(saleId = saleId, onBack = { navController.popBackStack() })
        }
    }
}

/**
 * Función que determina la ruta de inicio según el rol del usuario.
 * Se ha corregido para ser insensible a mayúsculas y minúsculas.
 */
private fun getStartRouteForRole(role: String): String {
    // Convierte el rol a minúsculas para una comparación robusta.
    return when (role.lowercase()) {
        "admin" -> "authors"
        "consultant" -> "products"
        "seller" -> "products"
        else -> "about"
    }
}

/**
 * Composable que contiene el Scaffold con la barra de navegación inferior.
 * Se encarga de la navegación dentro de las pantallas de la barra inferior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBottomBar(navController: NavHostController, userRole: String) {
    // 🎯 Se crea un NavController local para la navegación de la barra inferior.
    val bottomBarNavController = rememberNavController()

    // Se selecciona la lista de items correcta según el rol.
    val items = when (userRole.lowercase()) {
        "admin" -> adminNavItems
        "consultant" -> consultantNavItems
        "seller" -> sellerNavItems
        else -> otherNavItems
    }

    // Se determina la ruta de inicio del NavHost anidado.
    val startDestination = getStartRouteForRole(userRole)

    Scaffold(
        bottomBar = {
            NavigationBar {
                // Obtenemos la ruta actual del NavHost anidado.
                val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon?.let { icon -> Icon(icon, contentDescription = null) } },
                        label = { Text(screen.label) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            // 🎯 Navegamos usando el NavController local de la barra inferior.
                            bottomBarNavController.navigate(screen.route) {
                                popUpTo(bottomBarNavController.graph.findStartDestination().id) {
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
        // 🎯 EL NAVHOST ANIDADO:
        // Se coloca un nuevo NavHost dentro del Scaffold para manejar el contenido de la barra inferior.
        NavHost(
            navController = bottomBarNavController, // Se usa el NavController local
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Se definen las rutas para todas las pantallas de la barra inferior.
            composable("authors") { AuthorsScreen(navController) }
            composable("genres") { GenresScreen(navController) }
            composable("publishers") { PublishersScreen(navController) }
            composable("products") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                ProductsScreen(navController, canEdit)
            }
            composable("users") { UsersScreen(navController) }
            composable("roles") { RolesScreen(navController) }
            composable("sales") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                SalesScreen(navController, canEdit)
            }
            composable("about") { AboutScreen() }
        }
    }
}
