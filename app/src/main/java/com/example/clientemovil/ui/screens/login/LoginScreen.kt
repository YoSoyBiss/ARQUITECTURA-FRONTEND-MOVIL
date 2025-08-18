package com.example.clientemovil.ui.screens.login


import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions // Necesario para tipo de input
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType // Para tipo de input Email y Password
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.UserLoginRequest
// Removí LaravelRetrofitClient ya que no se usa en este archivo específico
// import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch

// Importa TUS colores específicos desde tu archivo theme
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.LightGrayBg

/**
 * Pantalla de inicio de sesión.
 * @param onLoginSuccess Función a llamar cuando el login es exitoso, pasando el rol del usuario.
 * @param onRegisterClick Función a llamar cuando el usuario quiere registrarse.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    // Definición de colores para los componentes
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = LightGrayBg,
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f),
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        focusedContainerColor = WhiteCard, // Fondo del TextField
        unfocusedContainerColor = WhiteCard, // Fondo del TextField
        disabledContainerColor = LightGrayBg // Si llegara a estar deshabilitado
    )

    val mainButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrownBorder,
        contentColor = WhiteCard,
        disabledContainerColor = LightGrayBg,
        disabledContentColor = BlackText.copy(alpha = 0.5f)
    )

    val textButtonColors = ButtonDefaults.textButtonColors(
        contentColor = BrownBorder, // Color del texto para el botón de registro
        disabledContentColor = LightGrayBg.copy(alpha = 0.7f)
    )

    // El Scaffold no es estrictamente necesario aquí si solo quieres un Column con color de fondo
    // pero si necesitaras TopAppBar o FAB, lo mantendrías.
    // Por simplicidad y para aplicar el fondo general, usaré Box o Column con background.

    Box( // Usamos Box para poder aplicar el color de fondo a toda la pantalla
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground) // Color de fondo principal
            .padding(16.dp), // Padding general de la pantalla
        contentAlignment = Alignment.Center // Centra el Column interior
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth() // La columna ocupa el ancho disponible dentro del Box
                // .padding(16.dp), // Movido al Box exterior
                .wrapContentHeight(), // Ajusta la altura al contenido
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = BlackText // Color del título
            )
            Spacer(Modifier.height(32.dp))

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
                        isLoading = true
                        try {
                            val response = NodeRetrofitClient.api.loginUser(UserLoginRequest(email, password))
                            if (response.isSuccessful) {
                                val userRole = response.body()?.user?.role
                                if (userRole != null) {
                                    onLoginSuccess(userRole)
                                } else {
                                    // Manejar caso donde userRole es null pero la respuesta fue exitosa
                                    Toast.makeText(context, "Rol de usuario no encontrado.", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                val message = response.errorBody()?.string() ?: "Error de credenciales"
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
                        } finally {
                            isLoading = false
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && email.isNotBlank() && password.isNotBlank(), // Deshabilitar si campos vacíos
                colors = mainButtonColors
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp), // Tamaño del indicador
                        color = WhiteCard, // Color del indicador dentro del botón (contraste con BrownBorder)
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Entrar")
                }
            }
            Spacer(Modifier.height(16.dp))
            TextButton(
                onClick = onRegisterClick,
                enabled = !isLoading, // También deshabilitar durante la carga
                colors = textButtonColors
            ) {
                Text("¿No tienes cuenta? Regístrate aquí")
            }
        }
    }
}

