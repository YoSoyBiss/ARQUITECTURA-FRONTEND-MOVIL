package com.example.clientemovil.ui.screens.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Role
import com.example.clientemovil.models.UserWithRoleObject // <- Importación correcta
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserFormScreen(
    userId: String? = null,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = userId != null && userId != "create"

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var availableRoles by remember { mutableStateOf<List<Role>>(emptyList()) }
    var selectedRole by remember { mutableStateOf<Role?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    suspend fun loadRoles() {
        try {
            val response = NodeRetrofitClient.api.getAllRoles()
            if (response.isSuccessful) {
                availableRoles = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            Log.e("UserFormScreen", "Error al cargar roles", e)
        }
    }

    suspend fun loadUser(id: String) {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getUser(id)
            if (response.isSuccessful) {
                val user = response.body()
                user?.let {
                    name = it.name
                    email = it.email
                    selectedRole = it.role
                }
            } else {
                Toast.makeText(context, "Error al cargar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadRoles()
            if (isEditing && userId != null) {
                loadUser(userId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Editar Usuario" else "Crear Usuario") },
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
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = selectedRole?.name ?: "Seleccionar Rol",
                            onValueChange = {},
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            availableRoles.forEach { roleOption ->
                                DropdownMenuItem(
                                    text = { Text(roleOption.name) },
                                    onClick = {
                                        selectedRole = roleOption
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val user = UserWithRoleObject(_id = userId, name = name, email = email, password = password, role = selectedRole)
                                if (isEditing) {
                                    updateUser(user, context)
                                } else {
                                    createUser(user, context)
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && email.isNotBlank() && selectedRole != null
                    ) {
                        Text(text = if (isEditing) "Guardar Cambios" else "Crear Usuario")
                    }
                }
            }
        }
    }
}

private suspend fun createUser(user: UserWithRoleObject, context: android.content.Context) {
    try {
        val response = NodeRetrofitClient.api.createUser(user)
        if (response.isSuccessful) {
            Toast.makeText(context, "Usuario creado con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al crear usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private suspend fun updateUser(user: UserWithRoleObject, context: android.content.Context) {
    try {
        val response = NodeRetrofitClient.api.updateUser(user._id!!, user)
        if (response.isSuccessful) {
            Toast.makeText(context, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al actualizar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}