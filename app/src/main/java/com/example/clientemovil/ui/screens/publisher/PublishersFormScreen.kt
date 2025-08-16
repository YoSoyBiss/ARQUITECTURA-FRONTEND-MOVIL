package com.example.clientemovil.ui.screens.publisher


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
fun PublishersFormScreen(
    publisherId: Int?,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = publisherId != null

    var publisherName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(publisherId) {
        if (isEditing) {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getPublisher(publisherId!!)
                if (response.isSuccessful) {
                    publisherName = response.body()?.name ?: ""
                } else {
                    Toast.makeText(context, "Error al cargar editorial", Toast.LENGTH_SHORT).show()
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
                title = { Text(if (isEditing) "Editar Editorial" else "Nueva Editorial") },
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
                        value = publisherName,
                        onValueChange = { publisherName = it },
                        label = { Text("Nombre de la editorial") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                if (isEditing) {
                                    try {
                                        val response = LaravelRetrofitClient.api.updatePublisher(
                                            publisherId!!,
                                            CatalogRequest(name = publisherName)
                                        )
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Editorial actualizada", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        } else {
                                            Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Error de red", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    try {
                                        val response = LaravelRetrofitClient.api.createPublisher(
                                            CatalogRequest(name = publisherName)
                                        )
                                        if (response.isSuccessful) {
                                            Toast.makeText(context, "Editorial creada", Toast.LENGTH_SHORT).show()
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
                        enabled = publisherName.isNotBlank()
                    ) {
                        Text(if (isEditing) "Guardar Cambios" else "Crear Editorial")
                    }
                }
            }
        }
    }
}
