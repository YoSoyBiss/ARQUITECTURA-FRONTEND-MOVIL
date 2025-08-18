package com.example.clientemovil.ui.screens.catalogos // O el paquete que uses

import android.util.Log
import android.widget.Toast
// Asegúrate de tener esta importación si no la tienes ya:
import java.util.UUID
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color // Mantén esta si usas colores específicos para SwipeToDismissBox
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// import com.example.clientemovil.Screen // Quita si no lo usas directamente aquí
import com.example.clientemovil.models.Publisher
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublishersScreen(navController: NavController) {
    var publishers by remember { mutableStateOf<List<Publisher>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var publisherToDelete by remember { mutableStateOf<Publisher?>(null) }

    val loadPublishers = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getAllPublishers()
                if (response.isSuccessful) {
                    publishers = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar editoriales: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(context, "Error HTTP: ${e.message()}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    val deletePublisher = { publisher: Publisher ->
        coroutineScope.launch {
            try {
                val response = LaravelRetrofitClient.api.deletePublisher(publisher.id!!) // Asegúrate de que el ID no sea nulo
                if (response.isSuccessful) {
                    Toast.makeText(context, "Editorial eliminada", Toast.LENGTH_SHORT).show()
                    loadPublishers() // Recarga la lista
                } else {
                    Toast.makeText(context, "Error al eliminar editorial: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) { // Captura genérica para otros errores
                Toast.makeText(context, "Error de red al eliminar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPublishers()
    }

    Scaffold(
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text("Editoriales", color = WhiteCard) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("publishers_form/new") },
                containerColor = BrownBorder,
                contentColor = WhiteCard
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Editorial")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrownBorder)
            } else if (publishers.isEmpty()) {
                Text("No hay editoriales registradas.", color = BlackText)
            }
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp) // Un poco más de espacio
                ) {
                    items(publishers, key = { it.id ?: UUID.randomUUID() }) { publisher -> // Usar UUID para keys si el ID puede ser nulo inicialmente
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> { // Deslizar de derecha a izquierda para borrar
                                        publisherToDelete = publisher
                                        true // Indica que el cambio de estado es aceptado
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> { // Deslizar de izquierda a derecha para editar
                                        publisher.id?.let {
                                            navController.navigate("publishers_form/${it}")
                                        }
                                        false // Para que el item vuelva a su posición original después de navegar
                                    }
                                    SwipeToDismissBoxValue.Settled -> false
                                }
                            },
                            // Positional threshold: cuánto debe deslizar el usuario para que se active una acción
                            positionalThreshold = { it * .25f }
                        )

                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Color(0xFF4CAF50) // Verde para editar
                                    SwipeToDismissBoxValue.EndToStart -> Color(0xFFF44336) // Rojo para borrar
                                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                                }
                                val alignment = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    SwipeToDismissBoxValue.Settled -> Alignment.Center
                                }
                                val text = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> "Editar"
                                    SwipeToDismissBoxValue.EndToStart -> "Borrar"
                                    SwipeToDismissBoxValue.Settled -> ""
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp), // Padding horizontal para el texto
                                    contentAlignment = alignment
                                ) {
                                    Text(text, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            },
                            content = {
                                ElevatedCard(
                                    onClick = {
                                        // Navegar al formulario de edición si se hace clic en la tarjeta
                                        // Opcionalmente, puedes deshabilitar esto si prefieres solo la acción de deslizar
                                        // publisher.id?.let { navController.navigate("publishers_form/${it}") }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = WhiteCard
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
                                ) {
                                    Box( // Usar Box para controlar mejor el padding interno
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 20.dp) // Padding más generoso
                                    ) {
                                        Text(
                                            text = publisher.name,
                                            style = MaterialTheme.typography.titleMedium, // Un poco más grande
                                            fontWeight = FontWeight.SemiBold,
                                            color = BlackText
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (publisherToDelete != null) {
        AlertDialog(
            containerColor = WhiteCard,
            onDismissRequest = { publisherToDelete = null },
            title = { Text("Confirmar Borrado", color = BrownBorder) },
            text = { Text("¿Seguro que quieres borrar la editorial \"${publisherToDelete!!.name}\"?", color = BlackText) },
            confirmButton = {
                Button(
                    onClick = {
                        publisherToDelete?.let { deletePublisher(it) }
                        publisherToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrownBorder,
                        contentColor = WhiteCard
                    )
                ) {
                    Text("Sí, Borrar")
                }
            },
            dismissButton = {
                OutlinedButton( // Usar OutlinedButton para el dismiss
                    onClick = { publisherToDelete = null },
                    border = ButtonDefaults.outlinedButtonBorder.copy(brush = SolidColor(BrownBorder)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = BrownBorder)
                ) {
                    Text("No")
                }
            }
        )
    }
}

// Para usar UUID en la key del LazyColumn si el ID puede ser nulo

