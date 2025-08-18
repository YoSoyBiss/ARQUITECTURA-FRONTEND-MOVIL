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
        // 1. LIMPIAR DATOS DE SESIÓN (SharedPreferences, ViewModel, token en cliente HTTP, etc.)
        //    ADAPTA ESTO A CÓMO GUARDAS LA SESIÓN EN TU APP.
        val sharedPreferences = context.getSharedPreferences("MyAppUserPrefs", Context.MODE_PRIVATE) // Usa el nombre correcto de tus SharedPreferences
        with(sharedPreferences.edit()) {
            remove("user_token") // La clave de tu token
            remove("user_role")  // La clave de tu rol
            // Cualquier otro dato de sesión que guardes
            apply()
        }
        // Si tu cliente Retrofit (NodeRetrofitClient) guarda el token estáticamente, límpialo también:
        // NodeRetrofitClient.clearAuthToken() // Necesitarías crear esta función en tu cliente HTTP

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // 2. NAVEGAR A LOGIN Y LIMPIAR BACKSTACK
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
                Surface(color = MaterialTheme.colorScheme.background) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val appNavController = rememberNavController() // NavController principal de la aplicación

    NavHost(
        navController = appNavController,
        startDestination = "login" // O una función que determine si ya hay sesión activa
    ) {
        // RUTAS DE LOGIN Y REGISTRO
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    // IMPORTANTE: Asegúrate de que en LoginScreen, ANTES de llamar a onLoginSuccess:
                    // 1. Guardes el token y el rol (ej. en SharedPreferences).
                    // 2. Configures tu cliente HTTP para usar el token.
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

        // RUTA PRINCIPAL (POST-LOGIN) QUE CONTIENE LA UI CON TOPAPPBAR Y BOTTOMNAVBAR
        composable("main_screen/{userRole}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("userRole")
            if (userRole != null) {
                MainScreenWithBars( // Renombrado para mayor claridad
                    appNavController = appNavController, // Pasa el NavController principal
                    userRole = userRole
                )
            }
            // Podrías añadir un else aquí para manejar el caso de userRole nulo,
            // por ejemplo, redirigiendo a login.
        }

        // RUTAS DE FORMULARIO (Se accede a ellas desde las pantallas de catálogo usando appNavController)
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

// Función para determinar la ruta de inicio de la BottomNavBar según el rol
private fun getStartRouteForRole(role: String): String {
    return when (role.lowercase()) {
        "admin" -> "authors"
        "consultant" -> "products"
        "seller" -> "products"
        else -> "about" // Ruta por defecto o para roles desconocidos
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreenWithBars( // Anteriormente MainScreenWithBottomBar
    appNavController: NavHostController, // NavController principal para navegar a Login o Formularios
    userRole: String
) {
    val bottomBarNavController = rememberNavController() // NavController para las pestañas de la BottomBar
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para el título de la TopAppBar
    var currentScreenTitle by remember { mutableStateOf("") }

    // Observar la entrada actual del backstack del NavController de la barra inferior
    val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
    val currentDestinationInBottomNav = navBackStackEntry?.destination

    // Seleccionar los ítems de la barra inferior según el rol
    val bottomNavItems = when (userRole.lowercase()) {
        "admin" -> adminNavItems
        "consultant" -> consultantNavItems
        "seller" -> sellerNavItems
        else -> otherNavItems
    }

    // Actualizar el título de la TopAppBar cuando cambia la pantalla en la barra inferior
    LaunchedEffect(currentDestinationInBottomNav) {
        currentScreenTitle = bottomNavItems.find { it.route == currentDestinationInBottomNav?.route }?.label ?: getStartRouteForRole(userRole).replaceFirstChar { it.uppercase() }
    }

    val startDestinationForBottomNav = getStartRouteForRole(userRole)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentScreenTitle) },
                actions = {
                    IconButton(onClick = {
                        // Opcional: Mostrar un diálogo de confirmación antes de llamar a performUserLogout
                        // ej. showConfirmLogoutDialog = true
                        performUserLogout(appNavController, scope, context)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Cerrar Sesión"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { screen.icon?.let { icon -> Icon(icon, contentDescription = screen.label) } },
                        label = { Text(screen.label) },
                        selected = currentDestinationInBottomNav?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
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
        // NavHost para el contenido de las pantallas de la barra inferior
        NavHost(
            navController = bottomBarNavController,
            startDestination = startDestinationForBottomNav,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Las pantallas de catálogo reciben appNavController para navegar a los formularios
            composable("authors") { AuthorsScreen(appNavController) }
            composable("genres") { GenresScreen(appNavController) }
            composable("publishers") { PublishersScreen(appNavController) }
            composable("products") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                ProductsScreen(appNavController, canEdit) // Pasa appNavController
            }
            composable("users") { UsersScreen(appNavController) }
            composable("roles") { RolesScreen(appNavController) }
            composable("sales") {
                val canEdit = userRole.lowercase() == "admin" || userRole.lowercase() == "seller"
                SalesScreen(appNavController, canEdit) // Pasa appNavController
            }
            composable("about") { AboutScreen() } // AboutScreen usualmente no necesita NavController o usa el local
        }
    }
}
