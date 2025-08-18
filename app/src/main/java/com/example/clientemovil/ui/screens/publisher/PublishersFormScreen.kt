package com.example.clientemovil.ui.screens.publisher

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.CatalogRequest
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishersFormScreen(
    publisherId: Int?,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = publisherId != null

    var publisherName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Define textFieldColors aquí, dentro del contexto composable
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = BrownBorder.copy(alpha = 0.7f),
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f), // O BrownBorder.copy(alpha = 0.7f)
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        disabledTextColor = BlackText.copy(alpha = 0.5f), // Color para texto deshabilitado
        disabledBorderColor = BrownBorder.copy(alpha = 0.3f),
        disabledLabelColor = BlackText.copy(alpha = 0.5f)
    )

    LaunchedEffect(publisherId) {
        if (isEditing && publisherId != null) { // Asegura que publisherId no es nulo aquí también
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getPublisher(publisherId) // No necesitas '!!' si ya lo comprobaste
                if (response.isSuccessful) {
                    publisherName = response.body()?.name ?: ""
                } else {
                    Toast.makeText(context, "Error al cargar editorial: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        } else {
            // Resetea el nombre si no estamos editando o si publisherId es nulo (ej. al navegar de editar a nuevo)
            publisherName = ""
            isLoading = false
        }
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Editorial" else "Nueva Editorial", color = WhiteCard) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = WhiteCard)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues) // Padding del Scaffold
                .padding(16.dp), // Padding adicional para el contenido interno
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrownBorder)
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth() // Para que la columna ocupe el ancho disponible
                        .align(Alignment.TopCenter) // Alinea la columna arriba, útil si el contenido no llena la pantalla
                        .padding(top = 16.dp), // Espacio adicional arriba si es necesario
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp) // Un poco más de espacio
                ) {
                    OutlinedTextField(
                        value = publisherName,
                        onValueChange = { publisherName = it },
                        label = { Text("Nombre de la editorial") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = textFieldColors, // Aplicar los colores definidos
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true // Mostrar indicador durante la operación de red
                                try {
                                    val response = if (isEditing) {
                                        LaravelRetrofitClient.api.updatePublisher(
                                            publisherId!!, // Aquí '!!' es más seguro si isEditing es true
                                            CatalogRequest(name = publisherName)
                                        )
                                    } else {
                                        LaravelRetrofitClient.api.createPublisher(
                                            CatalogRequest(name = publisherName)
                                        )
                                    }

                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Editorial ${if (isEditing) "actualizada" else "creada"}", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } else {
                                        val errorBody = response.errorBody()?.string() ?: "Respuesta sin cuerpo de error"
                                        Toast.makeText(context, "Error ${response.code()}: $errorBody", Toast.LENGTH_LONG).show()
                                    }
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                                } finally {
                                    isLoading = false // Ocultar indicador
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = publisherName.isNotBlank() && !isLoading, // Deshabilitar si está cargando
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BrownBorder,
                            contentColor = WhiteCard,
                            disabledContainerColor = BrownBorder.copy(alpha = 0.5f),
                            disabledContentColor = WhiteCard.copy(alpha = 0.7f)
                        )
                    ) {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Editorial")
                    }
                }
            }
        }
    }
}
