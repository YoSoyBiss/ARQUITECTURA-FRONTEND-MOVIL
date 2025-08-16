package com.example.clientemovil.ui.screens.genre


import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.CatalogRequest
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch

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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Género" else "Nuevo Género") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = genreName,
                        onValueChange = { genreName = it },
                        label = { Text("Nombre del género") },
                        modifier = Modifier.fillMaxWidth()
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
                        modifier = Modifier.fillMaxWidth(),
                        enabled = genreName.isNotBlank()
                    ) {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Género")
                    }
                }
            }
        }
    }
}
