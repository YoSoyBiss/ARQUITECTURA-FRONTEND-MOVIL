package com.example.clientemovil

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText // Lo necesitarás para el texto de los items no seleccionados
import com.example.clientemovil.ui.theme.ClienteMovilTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Data class para las pantallas
data class Screen(val route: String, val label: String, val icon: ImageVector? = null)

// Definiciones de los ítems de navegación
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

// --- INICIO: Lógica de Cierre de Sesión ---
fun performUserLogout(
    appNavController: NavHostController,
    scope: CoroutineScope,
    context: Context
) {
    scope.launch {
        val sharedPreferences = context.getSharedPreferences("MyAppUserPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("user_token")
            remove("user_role")
            apply()
        }
        // NodeRetrofitClient.clearAuthToken()
        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        appNavController.navigate("login") {
            popUpTo(appNavController.graph.id) {
                inclusive = true
            }
            launchSingleTop = true
        }
    }
}
// --- FIN: Lógica de Cierre de Sesión ---

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClienteMovilTheme {
                // El Surface aquí puede usar CreamBackground si quieres que sea el fondo por defecto
                // de las áreas no cubiertas por Scaffold, o mantener el del tema.
                Surface(color = CreamBackground) { // Aplicando color de fondo base
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val appNavController = rememberNavController()

    NavHost(
        navController = appNavController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    appNavController.navigate("main_screen/$role") {
                        popUpTo(appNavController.graph.id) { inclusive = true }
                    }
                },
                onRegisterClick = { appNavController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegistrationSuccess = { appNavController.popBackStack() }
            )
        }
        composable("main_screen/{userRole}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("userRole")
            if (userRole != null) {
                MainScreenWithBars(
                    appNavController = appNavController,
                    userRole = userRole
                )
            }
        }
        composable("authors_form/{authorId}") { backStackEntry ->
            val authorId = backStackEntry.arguments?.getString("authorId")?.toIntOrNull()
            AuthorsFormScreen(authorId = authorId, onBack = { appNavController.popBackStack() })
        }
        composable("genres_form/{genreId}") { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId")?.toIntOrNull()
            GenresFormScreen(genreId = genreId, onBack = { appNavController.popBackStack() })
        }
        composable("publishers_form/{publisherId}") { backStackEntry ->
            val publisherId = backStackEntry.arguments?.getString("publisherId")?.toIntOrNull()
            PublishersFormScreen(publisherId = publisherId, onBack = { appNavController.popBackStack() })
        }
        composable("products_form/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull()
            ProductsFormScreen(productId = productId, onBack = { appNavController.popBackStack() })
        }
        composable("user_form/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            UserFormScreen(userId = userId, onBack = { appNavController.popBackStack() })
        }
        composable("role_form/{roleId}") { backStackEntry ->
            val roleId = backStackEntry.arguments?.getString("roleId")
            RoleFormScreen(roleId = roleId, onBack = { appNavController.popBackStack() })
        }
        composable("sale_form/{saleId}") { backStackEntry ->
            val saleId = backStackEntry.arguments?.getString("saleId")
            SalesFormScreen(saleId = saleId, onBack = { appNavController.popBackStack() })
        }
    }
}

private fun getStartRouteForRole(role: String): String {
    return when (role.lowercase()) {
        "admin" -> "authors"
        "consultant" -> "products"
        "seller" -> "products"
        else -> "about"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBars(
    appNavController: NavHostController,
    userRole: String
) {
    val bottomBarNavController = rememberNavController()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var currentScreenTitle by remember { mutableStateOf("") }
    val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
    val currentDestinationInBottomNav = navBackStackEntry?.destination

    val bottomNavItems = when (userRole.lowercase()) {
        "admin" -> adminNavItems
        "consultant" -> consultantNavItems
        "seller" -> sellerNavItems
        else -> otherNavItems
    }

    LaunchedEffect(currentDestinationInBottomNav) {
        currentScreenTitle = bottomNavItems.find { it.route == currentDestinationInBottomNav?.route }?.label ?: getStartRouteForRole(userRole).replaceFirstChar { it.uppercase() }
    }

    val startDestinationForBottomNav = getStartRouteForRole(userRole)

    Scaffold(
        // Aplicando color de fondo al Scaffold principal también
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text(currentScreenTitle, color = WhiteCard) }, // Texto del título en blanco
                actions = {
                    IconButton(onClick = {
                        performUserLogout(appNavController, scope, context)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = WhiteCard // Icono de logout en blanco
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder, // Color de fondo de la TopAppBar
                    titleContentColor = WhiteCard, // Color del título
                    actionIconContentColor = WhiteCard // Color de los iconos de acción
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = BrownBorder, // Color de fondo de la NavigationBar
                contentColor = WhiteCard // Color por defecto para el contenido (útil para el indicador)
            ) {
                bottomNavItems.forEach { screen ->
                    val selected = currentDestinationInBottomNav?.hierarchy?.any { it.route == screen.route } == true
                    NavigationBarItem(
                        icon = {
                            screen.icon?.let { icon ->
                                Icon(
                                    icon,
                                    contentDescription = screen.label,
                                    // El color del icono cambiará según 'selectedColor' y 'unselectedColor' del item
                                )
                            }
                        },
                        label = {
                            Text(
                                screen.label,
                                // El color del texto cambiará según 'selectedColor' y 'unselectedColor' del item
                            )
                        },
                        selected = selected,
                        onClick = {
                            bottomBarNavController.navigate(screen.route) {
                                popUpTo(bottomBarNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = WhiteCard, // Icono seleccionado en blanco
                            selectedTextColor = WhiteCard, // Texto seleccionado en blanco
                            indicatorColor = WhiteCard.copy(alpha = 0.2f), // Color del indicador sutil (puedes ajustar)
                            unselectedIconColor = WhiteCard.copy(alpha = 0.7f), // Icono no seleccionado más tenue
                            unselectedTextColor = WhiteCard.copy(alpha = 0.7f)  // Texto no seleccionado más tenue
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomBarNavController,
            startDestination = startDestinationForBottomNav,
            modifier = Modifier
                .padding(innerPadding)
            // El fondo de las pantallas internas será CreamBackground debido al Scaffold
            // o puedes establecerlo explícitamente en cada pantalla si es necesario.
        ) {
            composable("authors") { AuthorsScreen(appNavController) }
            composable("genres") { GenresScreen(appNavController) }
            composable("publishers") { PublishersScreen(appNavController) }
            composable("products") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                ProductsScreen(appNavController, canEdit)
            }
            composable("users") { UsersScreen(appNavController) }
            composable("roles") { RolesScreen(appNavController) }
            composable("sales") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                SalesScreen(appNavController, canEdit)
            }
            composable("about") { AboutScreen() }
        }
    }
}
