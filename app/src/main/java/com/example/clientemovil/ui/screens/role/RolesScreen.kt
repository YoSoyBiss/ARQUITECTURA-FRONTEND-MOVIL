package com.example.clientemovil.ui.screens.role

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.models.Role
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
import com.example.clientemovil.ui.screens.more.Screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RolesScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var roles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var roleToDelete by remember { mutableStateOf<Role?>(null) }

    suspend fun loadRoles() {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getAllRoles()
            if (response.isSuccessful) {
                roles = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Error al cargar roles: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("RolesScreen", "Error al cargar roles", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadRoles()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Gestión de Roles") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.RoleForm.route.replace("{roleId}", "create")) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Rol")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                if (roles.isEmpty()) {
                    Text("No hay roles", Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(roles, key = { it._id ?: it.name }) { role ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            roleToDelete = role
                                            false
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            role._id?.let { navController.navigate(Screen.RoleForm.route.replace("{roleId}", it)) }
                                            false
                                        }
                                        else -> false
                                    }
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val color = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Color.Blue
                                        SwipeToDismissBoxValue.EndToStart -> Color.Red
                                        else -> Color.Transparent
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(16.dp),
                                        contentAlignment = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                            SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                            else -> Alignment.Center
                                        }
                                    ) {
                                        Text(
                                            text = when (dismissState.targetValue) {
                                                SwipeToDismissBoxValue.StartToEnd -> "Editar"
                                                SwipeToDismissBoxValue.EndToStart -> "Borrar"
                                                else -> ""
                                            },
                                            color = Color.White
                                        )
                                    }
                                },
                                content = {
                                    RoleItem(role = role, onClick = { /* TODO: Navegar a pantalla de edición de rol */ })
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (roleToDelete != null) {
        AlertDialog(
            onDismissRequest = { roleToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Seguro que quieres borrar el rol ${roleToDelete!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        roleToDelete?._id?.let {
                            val resp = NodeRetrofitClient.api.deleteRole(it)
                            if (resp.isSuccessful) {
                                loadRoles()
                            } else {
                                Log.e("RolesScreen", "Error al borrar rol: ${resp.code()}")
                            }
                        }
                        roleToDelete = null
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { roleToDelete = null }) {
                    Text("No")
                }
            }
        )
    }
}

@Composable
fun RoleItem(role: Role, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() } // Aunque usamos swipe, dejamos esta opción
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(text = "Nombre: ${role.name}", style = MaterialTheme.typography.titleMedium)
            role.description?.let {
                Text(text = "Descripción: $it", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}