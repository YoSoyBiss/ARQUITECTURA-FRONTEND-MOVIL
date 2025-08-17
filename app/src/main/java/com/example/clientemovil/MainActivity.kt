package com.example.clientemovil

import android.content.Context // Necesario para SharedPreferences y Toast
import android.os.Bundle
import android.widget.Toast // Necesario para el Toast
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
import androidx.compose.ui.platform.LocalContext // Necesario para obtener el Context
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
import kotlinx.coroutines.CoroutineScope // Necesario para launch
import kotlinx.coroutines.launch // Necesario para launch

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

// --- INICIO: Lógica de Cierre de Sesión ---
fun performUserLogout(
    appNavController: NavHostController, // El NavController principal de la app
    scope: CoroutineScope,
    context: Context
) {
    scope.launch {
        // 1. LIMPIAR DATOS DE SESIÓN (SharedPreferences, ViewModel, token en cliente HTTP, etc.)
        //    Este es un EJEMPLO. Adapta esto a CÓMO guardas la sesión.
        val sharedPreferences = context.getSharedPreferences("MyAppUserPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            remove("user_token") // El nombre de la key que usas para el token
            remove("user_role")  // El nombre de la key que usas para el rol
            // Cualquier otro dato de sesión que guardes
            apply()
        }
        // Si tu cliente Retrofit (NodeRetrofitClient) guarda el token estáticamente, límpialo también:
        // NodeRetrofitClient.clearAuthToken() // Necesitarías crear esta función en NodeRetrofitClient

        Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()

        // 2. NAVEGAR A LOGIN Y LIMPIAR BACKSTACK
        //    Usamos appNavController para volver a la pantalla de login.
        appNavController.navigate("login") { // Navega a la ruta de tu LoginScreen
            popUpTo(appNavController.graph.id) { // Limpia todo el grafo de navegación actual
                inclusive = true
            }
            launchSingleTop = true // Evita múltiples instancias de LoginScreen
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
    val navController = rememberNavController() // Este es el appNavController

    NavHost(
        navController = navController,
        startDestination = "login" // O una función que determine si ya hay sesión
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = { role ->
                    // Guardar rol y token en SharedPreferences ANTES de navegar si es necesario
                    // Ejemplo (deberías hacerlo dentro de LoginScreen al obtener la respuesta exitosa):
                    // val sharedPreferences = context.getSharedPreferences("MyAppUserPrefs", Context.MODE_PRIVATE)
                    // with(sharedPreferences.edit()) {
                    //    putString("user_role", role)
                    //    // putString("user_token", token_recibido_del_login)
                    //    apply()
                    // }
                    navController.navigate("main_screen/${role}") {
                        popUpTo(navController.graph.id) { inclusive = true }
                    }
                },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(onRegistrationSuccess = { navController.popBackStack() })
        }
        composable("main_screen/{userRole}") { backStackEntry ->
            val userRole = backStackEntry.arguments?.getString("userRole")
            if (userRole != null) {
                MainScreenWithBottomBar(
                    appNavController = navController, // Pasamos el NavController principal
                    userRole = userRole
                )
            }
        }
        // Rutas de formulario (se mantienen igual, ya que son navegadas por el appNavController)
        composable("authors_form/{authorId}") { /* ... */ }
        composable("genres_form/{genreId}") { /* ... */ }
        composable("publishers_form/{publisherId}") { /* ... */ }
        composable("products_form/{productId}") { /* ... */ }
        composable("user_form/{userId}") { /* ... */ }
        composable("role_form/{roleId}") { /* ... */ }
        composable("sale_form/{saleId}") { /* ... */ }
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
fun MainScreenWithBottomBar(
    appNavController: NavHostController, // Recibe el NavController principal
    userRole: String
) {
    val bottomBarNavController = rememberNavController() // Para la navegación interna de la BottomBar
    val scope = rememberCoroutineScope() // Para la corutina de logout
    val context = LocalContext.current // Para SharedPreferences y Toast en logout

    // Título dinámico para la TopAppBar
    var currentScreenTitle by remember { mutableStateOf("") }

    // Obtenemos la ruta actual del NavHost ANIDADO para actualizar el título y la selección de la BottomBar
    val navBackStackEntry by bottomBarNavController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Determinar los items de la BottomBar según el rol
    val bottomNavItems = when (userRole.lowercase()) {
        "admin" -> adminNavItems
        "consultant" -> consultantNavItems
        "seller" -> sellerNavItems
        else -> otherNavItems
    }

    // Actualizar el título cuando cambia la pantalla en el NavHost anidado
    LaunchedEffect(currentDestination) {
        currentScreenTitle = bottomNavItems.find { it.route == currentDestination?.route }?.label ?: "App"
    }

    val startDestinationForBottomNav = getStartRouteForRole(userRole)

    Scaffold(
        topBar = { // <-- AÑADIMOS LA TOPAPPBAR AQUÍ
            TopAppBar(
                title = { Text(currentScreenTitle) },
                actions = {
                    IconButton(onClick = {
                        // Opcional: Mostrar un diálogo de confirmación antes de llamar a performUserLogout
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
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
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
        NavHost(
            navController = bottomBarNavController,
            startDestination = startDestinationForBottomNav,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Las pantallas internas ahora usan el appNavController para navegar a los formularios
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

            // Las rutas de formulario ya NO se definen aquí, están en el NavHost principal (AppNavigation)
            // Esto es porque queremos que los formularios aparezcan ENCIMA de la UI con BottomBar/TopAppBar.
        }
    }
}

// Asegúrate de que las definiciones de tus pantallas de formulario (AuthorsFormScreen, etc.)
// y las pantallas de catálogo (AuthorsScreen, etc.) estén correctas
// y que acepten el NavController que se les pasa para cualquier navegación interna que necesiten.
// Por ejemplo, si AuthorsScreen necesita navegar a AuthorsFormScreen, usa el NavController
// que se le pasó (que es el appNavController).

