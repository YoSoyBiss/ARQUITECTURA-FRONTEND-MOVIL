package com.example.clientemovil.ui.screens.user

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete // Icono para borrar
import androidx.compose.material.icons.filled.Edit // Icono para editar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.network.node.NodeRetrofitClient
import com.example.clientemovil.ui.components.ListItem // Asumo que ListItem.UserItem está definido aquí
import com.example.clientemovil.ui.screens.more.Screen
// Importa tus colores personalizados si los tienes en un archivo separado
// import com.example.clientemovil.ui.theme.*
import kotlinx.coroutines.launch

// Define aquí tus colores si no los tienes en ui.theme
val CreamBackground = Color(0xFFFFF8E1) // Un crema ejemplo
val Brown = Color(0xFF795548)          // Un marrón ejemplo
val LightBrown = Color(0xFFA1887F)     // Un marrón claro ejemplo
val WhiteText = Color.White
val BlackText = Color.Black
val RedError = Color(0xFFD32F2F)       // Un rojo para errores
val GreenSuccess = Color(0xFF388E3C)   // Un verde para éxito (no usado directamente aquí pero útil)
val BlueAction = Color(0xFF1976D2)     // Un azul para acciones de edición

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
                    errorMessage = "Respuesta vacía del servidor."
                }
            } else {
                errorMessage = "Error al obtener usuarios: Código ${response.code()}"
            }
        } catch (e: Exception) {
            errorMessage = "No se pudo conectar al servidor: ${e.message}"
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
        containerColor = CreamBackground, // Color de fondo principal
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.UserForm.route.replace("{userId}", "create")) },
                containerColor = Brown, // Color de fondo del FAB
                contentColor = WhiteText  // Color del icono del FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar usuario")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Surface( // Surface ya no es estrictamente necesaria si Scaffold tiene containerColor, pero no hace daño.
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = CreamBackground // Asegura que el área de contenido también tenga este color
        ) {
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Brown) // Color del indicador
                    }
                }
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = errorMessage ?: "Error desconocido",
                            color = RedError, // Color para el mensaje de error
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                users.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No hay usuarios registrados.",
                            color = BlackText, // o LightBrown
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 8.dp), // Un poco de padding horizontal para las tarjetas
                        verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre tarjetas
                    ) {
                        items(users, key = { it.id ?: it.email }) { user ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> { // Swipe para borrar
                                            userToDelete = user
                                            false // No ocultar inmediatamente, esperar confirmación del diálogo
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> { // Swipe para editar
                                            user.id?.let {
                                                navController.navigate(
                                                    Screen.UserForm.route.replace("{userId}", it)
                                                )
                                            }
                                            false // No ocultar el item, solo navegar
                                        }
                                        else -> false
                                    }
                                },
                                positionalThreshold = { it * .25f } // Umbral para activar la acción
                            )

                            SwipeToDismissBox(
                                state = dismissState,
                                backgroundContent = {
                                    val direction = dismissState.dismissDirection
                                    val color = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> BlueAction.copy(alpha = 0.7f) // Editar
                                        SwipeToDismissBoxValue.EndToStart -> RedError.copy(alpha = 0.7f) // Borrar
                                        else -> Color.Transparent
                                    }
                                    val alignment = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    val icon = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> null
                                    }
                                    val text = when (direction) {
                                        SwipeToDismissBoxValue.StartToEnd -> "Editar"
                                        SwipeToDismissBoxValue.EndToStart -> "Borrar"
                                        else -> ""
                                    }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(color)
                                            .padding(horizontal = 20.dp),
                                        contentAlignment = alignment
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            if (direction == SwipeToDismissBoxValue.StartToEnd && icon != null) {
                                                Icon(icon, contentDescription = text, tint = WhiteText)
                                                Spacer(modifier = Modifier.width(8.dp))
                                            }
                                            Text(text = text, color = WhiteText, fontWeight = FontWeight.Bold)
                                            if (direction == SwipeToDismissBoxValue.EndToStart && icon != null) {
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Icon(icon, contentDescription = text, tint = WhiteText)
                                            }
                                        }
                                    }
                                },
                                content = { // Contenido de la tarjeta del usuario
                                    ElevatedCard(
                                        modifier = Modifier
                                            .fillMaxWidth(),
                                        colors = CardDefaults.elevatedCardColors(
                                            containerColor = Color.White // Fondo blanco para las tarjetas
                                        ),
                                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .padding(16.dp)
                                                .fillMaxWidth(),
                                            verticalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                "👤 ${user.name}",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = BlackText, // Color principal del texto
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "📧 ${user.email}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = BlackText.copy(alpha = 0.8f) // Un poco más claro
                                            )
                                            Text(
                                                "🔐 Rol: ${user.role}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = LightBrown // Usar marrón claro para el rol
                                            )
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
            containerColor = CreamBackground, // Color de fondo del diálogo
            titleContentColor = BlackText,   // Color del título
            textContentColor = BlackText,    // Color del texto
            onDismissRequest = { userToDelete = null },
            title = { Text("Confirmar borrado", fontWeight = FontWeight.Bold) },
            text = { Text("¿Seguro que quieres borrar al usuario '${userToDelete!!.name}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            userToDelete?.id?.let { id ->
                                val resp = NodeRetrofitClient.api.deleteUser(id)
                                if (resp.isSuccessful) {
                                    // Opcional: Mostrar un Toast de éxito
                                    loadUsers() // Recargar la lista
                                } else {
                                    errorMessage = "Error al borrar: ${resp.code()}"
                                    Log.e("UsersScreen", "Error al borrar usuario: ${resp.code()}")
                                }
                            }
                            userToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RedError, // Botón de confirmación en rojo
                        contentColor = WhiteText
                    )
                ) {
                    Text("Sí, borrar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { userToDelete = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Brown // Color del texto del botón de cancelar
                    )
                ) {
                    Text("No, cancelar")
                }
            }
        )
    }
}
