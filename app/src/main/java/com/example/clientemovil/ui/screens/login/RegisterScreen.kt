package com.example.clientemovil.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
// Importaciones de los modelos de datos. Asegúrate de que las rutas sean correctas.
import com.example.clientemovil.models.Role
import com.example.clientemovil.models.UserRegisterRequest
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

/**
 * Pantalla de registro de usuario.
 * @param onRegistrationSuccess Función a llamar cuando el registro es exitoso.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegistrationSuccess: () -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para manejar el ID del rol y el estado de carga
    var defaultRoleId by remember { mutableStateOf<String?>(null) }
    var rolesLoading by remember { mutableStateOf(true) }

    // Usamos LaunchedEffect para cargar el ID del rol al inicio del componente
    LaunchedEffect(Unit) {
        try {
            // Hacemos la llamada para obtener la lista de roles
            val response = NodeRetrofitClient.api.getAllRoles()

            if (response.isSuccessful) {
                // Buscamos el rol "Consultant" en la lista devuelta por el servidor
                val consultantRole = response.body()?.find { it.name == "consultant" }

                // Si encontramos el rol, guardamos su ID.
                defaultRoleId = consultantRole?._id

                if (defaultRoleId == null) {
                    Toast.makeText(context, "Error: Rol 'Consultant' no encontrado.", Toast.LENGTH_LONG).show()
                }

            } else {
                // Manejo de errores de la API
                val message = response.errorBody()?.string() ?: "Error al cargar los roles."
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            // Manejo de errores de red o excepciones
            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            // La carga ha terminado
            rolesLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Crear Cuenta", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Nombre Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                // Lanzamos la corutina para realizar el registro
                coroutineScope.launch {
                    isLoading = true
                    try {
                        // Usamos el defaultRoleId que obtuvimos de la API.
                        // El botón estará habilitado solo si defaultRoleId no es null.
                        val requestBody = UserRegisterRequest(name, email, password, defaultRoleId!!)
                        val response = NodeRetrofitClient.api.registerUser(requestBody)

                        if (response.isSuccessful) {
                            Toast.makeText(context, "Registro exitoso!", Toast.LENGTH_SHORT).show()
                            onRegistrationSuccess()
                        } else {
                            val message = response.errorBody()?.string() ?: "Error en el registro"
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    } finally {
                        isLoading = false
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            // El botón estará deshabilitado mientras se carga el ID del rol o si falta el ID
            enabled = !isLoading && !rolesLoading && defaultRoleId != null
        ) {
            if (isLoading || rolesLoading) {
                // Mostrar un indicador de progreso mientras se carga o se registra
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Crear Cuenta")
            }
        }
    }
}