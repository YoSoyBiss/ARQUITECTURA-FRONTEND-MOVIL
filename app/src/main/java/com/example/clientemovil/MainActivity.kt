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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.clientemovil.ui.screens.role.RoleFormScreen
import com.example.clientemovil.ui.screens.role.RolesScreen
import com.example.clientemovil.ui.screens.sales.SalesFormScreen
import com.example.clientemovil.ui.screens.sales.SalesScreen
import com.example.clientemovil.ui.screens.user.UserFormScreen
import com.example.clientemovil.ui.screens.user.UsersScreen
import com.example.clientemovil.ui.theme.ClienteMovilTheme

sealed class Screen(val route: String, val label: String, val icon: ImageVector) {
    object Users : Screen("users", "Usuarios", Icons.Default.Person)
    object Roles : Screen("roles", "Roles", Icons.Default.List)
    object Sales : Screen("sales", "Ventas", Icons.Default.ShoppingCart) // ¡NUEVO!
    object UserForm : Screen("user_form/{userId}", "UserForm", Icons.Default.Person)
    object RoleForm : Screen("role_form/{roleId}", "RoleForm", Icons.Default.List)
    object SaleForm : Screen("sale_form/{saleId}", "SaleForm", Icons.Default.ShoppingCart) // ¡NUEVO!
}

val items = listOf(Screen.Users, Screen.Roles, Screen.Sales)

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

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                items.forEach { screen ->
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
            startDestination = Screen.Users.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Users.route) { UsersScreen(navController) }
            composable(Screen.Roles.route) { RolesScreen(navController) }
            composable(Screen.Sales.route) { SalesScreen(navController) } // ¡NUEVO!
            composable(Screen.UserForm.route) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                UserFormScreen(
                    userId = userId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RoleForm.route) { backStackEntry ->
                val roleId = backStackEntry.arguments?.getString("roleId")
                RoleFormScreen(
                    roleId = roleId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.SaleForm.route) { backStackEntry -> // ¡NUEVO!
                val saleId = backStackEntry.arguments?.getString("saleId")
                SalesFormScreen(
                    saleId = saleId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ClienteMovilTheme {
        MainScreen()
    }
}