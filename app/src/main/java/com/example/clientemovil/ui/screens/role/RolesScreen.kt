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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // <- Necesitaremos Color.Red o colores de MaterialTheme
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.models.Role
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
import com.example.clientemovil.ui.screens.more.Screen
// Importa tus colores personalizados DISPONIBLES
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText

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
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text(text = "Gestión de Roles", color = WhiteCard) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder,
                    titleContentColor = WhiteCard
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.RoleForm.route.replace("{roleId}", "create")) },
                containerColor = BrownBorder,
                contentColor = WhiteCard
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
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BrownBorder
                )
            } else {
                if (roles.isEmpty()) {
                    Text(
                        "No hay roles",
                        Modifier.align(Alignment.Center),
                        color = BlackText
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(roles, key = { it._id ?: it.name }) { role ->
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> { // Borrar
                                            roleToDelete = role
                                            false
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> { // Editar
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
                                        // Usar un tono de BrownBorder para editar o un color estándar
                                        SwipeToDismissBoxValue.StartToEnd -> BrownBorder.copy(alpha = 0.7f)
                                        // Usar Color.Red estándar de Material para borrar
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.errorContainer
                                        else -> CreamBackground
                                    }
                                    val textColor = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> WhiteCard
                                        SwipeToDismissBoxValue.EndToStart -> MaterialTheme.colorScheme.onErrorContainer
                                        else -> BlackText
                                    }
                                    val alignment = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                    val text = when (dismissState.targetValue) {
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
                                        Text(text, color = textColor)
                                    }
                                },
                                content = {
                                    RoleItem(role = role, onClick = {
                                        role._id?.let { navController.navigate(Screen.RoleForm.route.replace("{roleId}", it)) }
                                    })
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
            containerColor = WhiteCard,
            onDismissRequest = { roleToDelete = null },
            title = { Text("Confirmar borrado", color = BrownBorder) },
            text = { Text("¿Seguro que quieres borrar el rol ${roleToDelete!!.name}?", color = BlackText) },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            roleToDelete?._id?.let {
                                val resp = NodeRetrofitClient.api.deleteRole(it)
                                if (resp.isSuccessful) {
                                    loadRoles()
                                } else {
                                    Log.e("RolesScreen", "Error al borrar rol: ${resp.code()}")
                                    Toast.makeText(context, "Error al borrar: ${resp.code()}", Toast.LENGTH_SHORT).show()
                                }
                            }
                            roleToDelete = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        // Usar el color de error del tema para el botón de confirmación de borrado
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { roleToDelete = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = BrownBorder
                    )
                ) {
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
            .clickable { onClick() },
        colors = CardDefaults.elevatedCardColors(
            containerColor = WhiteCard
        ),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Nombre: ${role.name}",
                style = MaterialTheme.typography.titleMedium,
                color = BlackText
            )
            role.description?.let {
                Text(
                    text = "Descripción: $it",
                    style = MaterialTheme.typography.bodySmall,
                    color = BlackText.copy(alpha = 0.7f)
                )
            }
        }
    }
}
