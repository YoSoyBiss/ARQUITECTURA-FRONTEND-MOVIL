package com.example.clientemovil.ui.screens.genre


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
import kotlinx.coroutines.launch

// Importa TUS colores específicos desde tu archivo theme
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.LightGrayBg

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresFormScreen(
    genreId: Int?,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = genreId != null

    var genreName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(genreId) {
        if (isEditing) {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getGenre(genreId!!)
                if (response.isSuccessful) {
                    genreName = response.body()?.name ?: ""
                } else {
                    Toast.makeText(context, "Error al cargar género", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

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
        unfocusedContainerColor = WhiteCard // Fondo del TextField
    )

    val mainButtonColors = ButtonDefaults.buttonColors(
        containerColor = BrownBorder,
        contentColor = WhiteCard,
        disabledContainerColor = LightGrayBg,
        disabledContentColor = BlackText.copy(alpha = 0.5f)
    )

    Scaffold(
        containerColor = CreamBackground, // Color de fondo principal de la pantalla
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Género" else "Nuevo Género", color = WhiteCard) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = WhiteCard // Color del icono de navegación
                        )
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
                .padding(paddingValues) // Aplicar padding del Scaffold
                .padding(16.dp), // Padding adicional para el contenido interno
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrownBorder) // Color del indicador de progreso
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Espacio entre el TextField y el Botón
                    modifier = Modifier.fillMaxWidth() // Asegura que la columna use el ancho disponible
                ) {
                    OutlinedTextField(
                        value = genreName,
                        onValueChange = { genreName = it },
                        label = { Text("Nombre del género") },
                        modifier = Modifier.fillMaxWidth(), // El TextField ocupa todo el ancho
                        colors = textFieldColors // Aplica los colores definidos
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (isEditing) {
                                    try {
                                        val response = LaravelRetrofitClient.api.updateGenre(
                                            genreId!!,
                                            CatalogRequest(name = genreName)
                                        )
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Género actualizado", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    try {
                                        val response = LaravelRetrofitClient.api.createGenre(
                                            CatalogRequest(name = genreName)
                                        )
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Género creado", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, "Error al crear", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(), // El Botón ocupa todo el ancho
                        enabled = genreName.isNotBlank(), // Habilitado solo si el nombre no está vacío
                        colors = mainButtonColors // Aplica los colores definidos para el botón
                    ) {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Género")
                    }
                }
            }
        }
    }
}
