package com.example.clientemovil.ui.screens.login

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
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
// Importaciones de los modelos de datos. Asegúrate de que las rutas sean correctas.
// import com.example.clientemovil.models.Role // No se usa directamente el modelo Role, solo su ID.
import com.example.clientemovil.models.UserRegisterRequest
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

// Importa TUS colores específicos desde tu archivo theme
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.LightGrayBg

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
    var isLoading by remember { mutableStateOf(false) } // Para la acción de registro
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estado para manejar el ID del rol y el estado de carga de roles
    var defaultRoleId by remember { mutableStateOf<String?>(null) }
    var rolesLoading by remember { mutableStateOf(true) } // Para la carga inicial de roles

    // Definición de colores para los componentes
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = LightGrayBg,
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f),
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        focusedContainerColor = WhiteCard,
        unfocusedContainerColor = WhiteCard,
        disabledContainerColor = LightGrayBg
    )

    val mainButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrownBorder,
        contentColor = WhiteCard,
        disabledContainerColor = LightGrayBg.copy(alpha = 0.7f), // Color cuando está deshabilitado
        disabledContentColor = BlackText.copy(alpha = 0.5f)
    )

    // Usamos LaunchedEffect para cargar el ID del rol "consultant" al inicio del componente
    LaunchedEffect(Unit) {
        rolesLoading = true // Inicia la carga de roles
        try {
            val response = NodeRetrofitClient.api.getAllRoles()
            if (response.isSuccessful) {
                val consultantRole = response.body()?.find { it.name == "consultant" }
                defaultRoleId = consultantRole?._id
                if (defaultRoleId == null) {
                    Toast.makeText(context, "Error: Rol 'consultant' no encontrado.", Toast.LENGTH_LONG).show()
                }
            } else {
                val message = response.errorBody()?.string() ?: "Error al cargar los roles."
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error de red al cargar roles: ${e.message}", Toast.LENGTH_LONG).show()
        } finally {
            rolesLoading = false // Termina la carga de roles
        }
    }

    Box( // Usamos Box para poder aplicar el color de fondo a toda la pantalla
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground) // Color de fondo principal
            .padding(16.dp), // Padding general
        contentAlignment = Alignment.Center // Centra el Column interior
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(), // Ajusta la altura al contenido
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Crear Cuenta",
                style = MaterialTheme.typography.headlineMedium,
                color = BlackText // Color del título
            )
            Spacer(Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Contraseña") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                colors = textFieldColors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true
            )
            Spacer(Modifier.height(24.dp)) // Un poco más de espacio antes del botón

            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoading = true // Inicia la carga del proceso de registro
                        try {
                            // defaultRoleId debe existir para proceder, ya validado por 'enabled'
                            val requestBody = UserRegisterRequest(name, email, password, defaultRoleId!!)
                            val response = NodeRetrofitClient.api.registerUser(requestBody)

                            if (response.isSuccessful) {
                                Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                                onRegistrationSuccess()
                            } else {
                                val message = response.errorBody()?.string() ?: "Error en el registro"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false // Termina la carga del proceso de registro
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && !rolesLoading && defaultRoleId != null && name.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                colors = mainButtonColors
            ) {
                // Mostrar indicador de progreso si isLoading (registro) o rolesLoading (carga inicial) es true
                if (isLoading || rolesLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = WhiteCard, // Color del indicador dentro del botón
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Crear Cuenta")
                }
            }
        }
    }
}

