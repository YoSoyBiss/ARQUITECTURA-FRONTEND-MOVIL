package com.example.clientemovil.ui.screens.role

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Role
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

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
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Editar Rol" else "Crear Rol") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
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
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Descripción") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            scope.launch {
                                val success = if (isEditing) {
                                    updateRole(roleId!!, name, description, context)
                                } else {
                                    createRole(name, description, context)
                                }
                                if (success) {
                                    onBack()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank()
                    ) {
                        Text(text = if (isEditing) "Guardar Cambios" else "Crear Rol")
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
            Toast.makeText(context, "Error al crear rol: ${response.code()}", Toast.LENGTH_SHORT).show()
            false
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}

private suspend fun updateRole(roleId: String, name: String, description: String, context: android.content.Context): Boolean {
    return try {
        val requestBody = Role(_id = roleId, name = name, description = description)
        // El error 'unresolved reference' que tenías en updateRole era porque no se estaba importando
        // correctamente. Asegúrate de que todas las dependencias estén correctas.
        val response = NodeRetrofitClient.api.updateRole(roleId, requestBody)
        if (response.isSuccessful) {
            Toast.makeText(context, "Rol actualizado con éxito", Toast.LENGTH_SHORT).show()
            true
        } else {
            Toast.makeText(context, "Error al actualizar rol: ${response.code()}", Toast.LENGTH_SHORT).show()
            false
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
        false
    }
}