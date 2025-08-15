package com.example.clientemovil.ui.screens.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.ui.components.ListItem
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
import com.example.clientemovil.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var users by remember { mutableStateOf<List<ListItem.UserItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var userToDelete by remember { mutableStateOf<ListItem.UserItem?>(null) }

    suspend fun loadUsers() {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getAllUsers()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    users = body.map { user ->
                        ListItem.UserItem(
                            id = user._id,
                            name = user.name,
                            email = user.email,
                            role = user.role?.name ?: "N/A"
                        )
                    }
                    errorMessage = null
                } else {
                    errorMessage = "Respuesta vacía"
                }
            } else {
                errorMessage = "Error: Código ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "Excepción: ${e.message}"
            Log.e("UsersScreen", "Error al obtener usuarios", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadUsers()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.UserForm.route.replace("{userId}", "create")) }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar usuario")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background
        ) {
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Text(text = errorMessage ?: "Error desconocido", color = MaterialTheme.colorScheme.error)
                    }
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(users, key = { it.id ?: it.email }) { user ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            userToDelete = user
                                            false
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            user.id?.let { navController.navigate(Screen.UserForm.route.replace("{userId}", it)) }
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
                                            SwipeToDismissBoxValue.StartToEnd -> androidx.compose.ui.Alignment.CenterStart
                                            SwipeToDismissBoxValue.EndToStart -> androidx.compose.ui.Alignment.CenterEnd
                                            else -> androidx.compose.ui.Alignment.Center
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
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("👤 ${user.name}", style = MaterialTheme.typography.titleMedium)
                                            Text("📧 ${user.email}")
                                            Text("🔐 ${user.role}")
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (userToDelete != null) {
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Seguro que quieres borrar a ${userToDelete!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch {
                        userToDelete?.id?.let {
                            val resp = NodeRetrofitClient.api.deleteUser(it)
                            if (resp.isSuccessful) {
                                loadUsers()
                            } else {
                                Log.e("UsersScreen", "Error al borrar usuario: ${resp.code()}")
                            }
                        }
                        userToDelete = null
                    }
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("No")
                }
            }
        )
    }
}