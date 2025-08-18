package com.example.clientemovil.ui.screens.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background // Necesario para ExposedDropdownMenu
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Role
import com.example.clientemovil.models.User
import com.example.clientemovil.models.UserWithRoleObject
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

// Importa TUS colores específicos desde tu archivo theme
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.LightGrayBg
// Si necesitas RedError, defínelo en tu Color.kt o usa un color existente
// import com.example.clientemovil.ui.theme.RedError // Si lo tienes

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

    // Estados para el diálogo de cambio de contraseña
    var showPasswordDialog by remember { mutableStateOf(false) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

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
            val response = NodeRetrofitClient.api.getUserWithRoleString(id)
            if (response.isSuccessful) {
                val user = response.body()
                user?.let {
                    name = it.name
                    email = it.email
                    selectedRole = availableRoles.find { role -> role._id == it.role }
                }
                Log.d("UserFormScreen", "Usuario cargado: ${user?.name}")
            } else {
                Toast.makeText(context, "Error al cargar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
                Log.e("UserFormScreen", "Error al cargar usuario: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("UserFormScreen", "Excepción al cargar usuario", e)
        } finally {
            isLoading = false
        }
    }

    suspend fun updatePassword(currentPass: String, newPass: String) {
        if (currentPass.isEmpty() || newPass.isEmpty()) {
            Toast.makeText(context, "Los campos no pueden estar vacíos", Toast.LENGTH_SHORT).show()
            return
        }

        if (userId == null) {
            Toast.makeText(context, "ID de usuario no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = mapOf(
            "currentPassword" to currentPass,
            "newPassword" to newPass
        )

        try {
            val response = NodeRetrofitClient.api.updatePassword(userId, requestBody)

            if (response.isSuccessful) {
                Toast.makeText(context, "Contraseña actualizada con éxito", Toast.LENGTH_SHORT).show()
                showPasswordDialog = false
            } else {
                val errorMessage = if (response.code() == 401) {
                    "Contraseña actual incorrecta."
                } else {
                    "Error al actualizar la contraseña: ${response.code()}"
                }
                Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción al actualizar contraseña: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("UserFormScreen", "Excepción al actualizar contraseña", e)
        }
    }

    LaunchedEffect(userId) {
        scope.launch {
            loadRoles()
            if (isEditing && userId != null) {
                loadUser(userId)
            }
        }
    }

    // Definición de colores para los TextFields y otros componentes
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = LightGrayBg, // o BrownBorder.copy(alpha = 0.7f)
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f),
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        focusedContainerColor = WhiteCard, // Fondo del TextField
        unfocusedContainerColor = WhiteCard, // Fondo del TextField
        disabledContainerColor = LightGrayBg,
        focusedTrailingIconColor = BrownBorder,
        unfocusedTrailingIconColor = LightGrayBg
    )

    val mainButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrownBorder,
        contentColor = WhiteCard,
        disabledContainerColor = LightGrayBg, // Color cuando está deshabilitado
        disabledContentColor = BlackText.copy(alpha = 0.5f) // Color del texto cuando está deshabilitado
    )

    val secondaryButtonColors = ButtonDefaults.buttonColors( // Para "Modificar Contraseña"
        containerColor = LightGrayBg, // o un BrownBorder más claro
        contentColor = BlackText // o WhiteCard si el fondo es más oscuro
    )


    Scaffold(
        containerColor = CreamBackground, // Color de fondo principal de la pantalla
        topBar = {
            TopAppBar(
                title = { Text(text = if (isEditing) "Editar Usuario" else "Crear Usuario", color = WhiteCard) },
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
                .padding(16.dp) // Padding interno del contenido
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
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        colors = textFieldColors
                    )
                    if (!isEditing) {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            visualTransformation = PasswordVisualTransformation(),
                            colors = textFieldColors
                        )
                    } else {
                        Button(
                            onClick = { showPasswordDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = secondaryButtonColors // Botón secundario para modificar contraseña
                        ) {
                            Text("Modificar Contraseña")
                        }
                    }

                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            readOnly = true,
                            value = selectedRole?.name ?: "Seleccionar Rol",
                            onValueChange = {},
                            label = { Text("Rol") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = textFieldColors // Usa los mismos colores para consistencia
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.background(WhiteCard) // Fondo del menú desplegable
                        ) {
                            availableRoles.forEach { roleOption ->
                                DropdownMenuItem(
                                    text = { Text(roleOption.name, color = BlackText) }, // Color del texto del item
                                    onClick = {
                                        selectedRole = roleOption
                                        expanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                                    colors = MenuDefaults.itemColors(
                                        textColor = BlackText, // Color del texto del item
                                        // Puedes definir colores para leadingIconColor, trailingIconColor si los usas
                                    )
                                )
                            }
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                val user = UserWithRoleObject(
                                    _id = if (isEditing) userId else null,
                                    name = name,
                                    email = email,
                                    password = if (isEditing) null else password,
                                    role = selectedRole
                                )
                                if (isEditing) {
                                    updateUser(user, context)
                                } else {
                                    createUser(user, context)
                                }
                                onBack()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = name.isNotBlank() && email.isNotBlank() && selectedRole != null,
                        colors = mainButtonColors // Botón principal
                    ) {
                        Text(text = if (isEditing) "Guardar Cambios" else "Crear Usuario")
                    }
                }
            }
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            containerColor = CreamBackground, // Color de fondo del diálogo
            titleContentColor = BlackText, // Color del título del diálogo
            textContentColor = BlackText, // Color del texto del contenido del diálogo
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Modificar Contraseña") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Contraseña Actual") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = textFieldColors // Mismos colores para los campos
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("Nueva Contraseña") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation(),
                        colors = textFieldColors // Mismos colores para los campos
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            updatePassword(currentPassword, newPassword)
                            currentPassword = ""
                            newPassword = ""
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón de confirmación
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón de cancelar
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Funciones auxiliares para crear y actualizar usuario (se mantienen igual)
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

// Archivo: ui/screens/user/UserFormScreen.kt

private suspend fun updateUser(user: UserWithRoleObject, context: android.content.Context) {
    try {
        // Crea un mapa con los datos que quieres enviar
        val requestBody = mapOf(
            "name" to user.name,
            "email" to user.email,
            "role" to user.role?._id.orEmpty() // Asegura que el valor sea un String
        )

        val response = NodeRetrofitClient.api.updateUser(user._id!!, requestBody)
        if (response.isSuccessful) {
            Toast.makeText(context, "Usuario actualizado con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al actualizar usuario: ${response.code()}", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("UserFormScreen", "Error en updateUser", e) // Agrega un log para depurar
    }
}
