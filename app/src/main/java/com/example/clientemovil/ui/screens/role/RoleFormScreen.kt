package com.example.clientemovil.ui.screens.role

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Role
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleFormScreen(
    roleId: String? = null,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = roleId != null && roleId != "create"

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Colores para OutlinedTextField
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = BrownBorder.copy(alpha = 0.7f),
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f),
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        disabledTextColor = BlackText.copy(alpha = 0.5f),
        disabledBorderColor = BrownBorder.copy(alpha = 0.3f),
        disabledLabelColor = BlackText.copy(alpha = 0.5f),
        // Color del contenedor del TextField (fondo)
        focusedContainerColor = WhiteCard, // Fondo cuando está enfocado
        unfocusedContainerColor = WhiteCard, // Fondo cuando no está enfocado
        disabledContainerColor = WhiteCard.copy(alpha = 0.7f) // Fondo cuando está deshabilitado
    )

    suspend fun loadRole(id: String) {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getRole(id)
            if (response.isSuccessful) {
                val role = response.body()
                role?.let {
                    name = it.name
                    description = it.description ?: ""
                }
            } else {
                Toast.makeText(context, "Error al cargar rol: ${response.code()}", Toast.LENGTH_SHORT).show()
                Log.e("RoleFormScreen", "Error al cargar rol: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("RoleFormScreen", "Excepción al cargar rol", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(roleId) {
        if (isEditing && roleId != null) {
            loadRole(roleId)
        }
    }

    Scaffold(
        containerColor = CreamBackground, // Color de fondo del Scaffold
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Editar Rol" else "Crear Rol", color = WhiteCard) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = WhiteCard)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder, // Color de fondo de la TopAppBar
                    titleContentColor = WhiteCard, // Color del título
                    navigationIconContentColor = WhiteCard // Color del icono de navegación
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
            // CreamBackground ya está aplicado por el Scaffold
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = BrownBorder // Color del indicador de progreso
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nombre del Rol") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors, // Aplicar colores personalizados
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors, // Aplicar colores personalizados
                        singleLine = false, // Permite múltiples líneas para la descripción
                        maxLines = 5
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true // Mostrar indicador mientras se procesa
                                val success = if (isEditing) {
                                    updateRole(roleId!!, name, description, context)
                                } else {
                                    createRole(name, description, context)
                                }
                                isLoading = false // Ocultar indicador
                                if (success) {
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && !isLoading, // Deshabilitar si está cargando
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrownBorder,
                            contentColor = WhiteCard,
                            disabledContainerColor = BrownBorder.copy(alpha = 0.5f),
                            disabledContentColor = WhiteCard.copy(alpha = 0.7f)
                        )
                    ) {
                        if (isLoading && !isEditing) { // Mostrar indicador solo en creación si el botón se presiona
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = WhiteCard, strokeWidth = 2.dp)
                        } else {
                            Text(text = if (isEditing) "Guardar Cambios" else "Crear Rol")
                        }
                    }
                }
            }
        }
    }
}

private suspend fun createRole(name: String, description: String, context: android.content.Context): Boolean {
    return try {
        val requestBody = Role(name = name, description = description)
        val response = NodeRetrofitClient.api.createRole(requestBody)
        if (response.isSuccessful) {
            Toast.makeText(context, "Rol creado con éxito", Toast.LENGTH_SHORT).show()
            true
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Toast.makeText(context, "Error al crear rol: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
            Log.e("RoleFormScreen", "Error al crear rol: ${response.code()} - $errorBody")
            false
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción al crear rol: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("RoleFormScreen", "Excepción al crear rol", e)
        false
    }
}

private suspend fun updateRole(roleId: String, name: String, description: String, context: android.content.Context): Boolean {
    return try {
        val requestBody = Role(_id = roleId, name = name, description = description)
        val response = NodeRetrofitClient.api.updateRole(roleId, requestBody)
        if (response.isSuccessful) {
            Toast.makeText(context, "Rol actualizado con éxito", Toast.LENGTH_SHORT).show()
            true
        } else {
            val errorBody = response.errorBody()?.string() ?: "Error desconocido"
            Toast.makeText(context, "Error al actualizar rol: ${response.code()} - $errorBody", Toast.LENGTH_LONG).show()
            Log.e("RoleFormScreen", "Error al actualizar rol: ${response.code()} - $errorBody")
            false
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción al actualizar rol: ${e.message}", Toast.LENGTH_LONG).show()
        Log.e("RoleFormScreen", "Excepción al actualizar rol", e)
        false
    }
}
