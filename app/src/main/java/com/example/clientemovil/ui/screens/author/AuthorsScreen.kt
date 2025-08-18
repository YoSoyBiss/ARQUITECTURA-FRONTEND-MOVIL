package com.example.clientemovil.ui.screens.catalogos

import android.util.Log
import android.widget.Toast
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
import androidx.compose.ui.graphics.Color // Import general de Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
// Removí la importación de Screen ya que no se usa en este archivo específico.
// import com.example.clientemovil.Screen
import com.example.clientemovil.models.Author
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Importa TUS colores específicos desde tu archivo theme
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.WhiteCard
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.LightGrayBg
// Necesitarás definir colores para las acciones de swipe (editar y borrar)
// Por ejemplo, puedes añadir estos a tu Color.kt:
// val SwipeEditBackground = Color(0xFF4CAF50) // Un verde para editar
// val SwipeDeleteBackground = Color(0xFFF44336) // Un rojo para borrar
// O usar los colores que prefieras. Para este ejemplo, usaré unos genéricos.
val SwipeEditColor = Color.Blue // Puedes cambiarlo por tu color de tema
val SwipeDeleteColor = Color.Red // Puedes cambiarlo por tu color de tema


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthorsScreen(navController: NavController) {
    var authors by remember { mutableStateOf<List<Author>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var authorToDelete by remember { mutableStateOf<Author?>(null) }

    val loadAuthors = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getAllAuthors()
                if (response.isSuccessful) {
                    authors = response.body() ?: emptyList()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("AuthorsScreen", "Error al cargar autores. Código: ${response.code()}, Mensaje: $errorBody")
                    Toast.makeText(context, "Error al cargar autores: Código ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("AuthorsScreen", "Error de red", e)
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: HttpException) {
                Log.e("AuthorsScreen", "Error HTTP", e)
                Toast.makeText(context, "Error HTTP: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    val deleteAuthor = { author: Author ->
        coroutineScope.launch {
            try {
                val response = LaravelRetrofitClient.api.deleteAuthor(author.id!!)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Autor eliminado", Toast.LENGTH_SHORT).show()
                    loadAuthors() // Recarga la lista
                } else {
                    Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadAuthors()
    }

    Scaffold(
        containerColor = CreamBackground, // Color de fondo principal
        topBar = {
            TopAppBar(
                title = { Text("Autores", color = WhiteCard) }, // Texto del título en blanco
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder, // Color de fondo de la TopAppBar
                    titleContentColor = WhiteCard // Asegura que el color del título sea el deseado
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate("authors_form/new")
                },
                containerColor = BrownBorder, // Color de fondo del FAB
                contentColor = WhiteCard // Color del icono dentro del FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Autor")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues), // Aplicar padding del Scaffold
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrownBorder) // Color del indicador de progreso
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp), // Padding para el contenido de la lista
                    verticalArrangement = Arrangement.spacedBy(8.dp) // Espacio entre items
                ) {
                    items(authors, key = { it.id ?: 0 }) { author ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> { // Swipe para borrar
                                        authorToDelete = author
                                        false // No desaparece inmediatamente, muestra diálogo
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> { // Swipe para editar
                                        author.id?.let {
                                            navController.navigate("authors_form/${it}")
                                        }
                                        false // No desaparece, solo navega
                                    }
                                    else -> false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> SwipeEditColor // Color para editar
                                    SwipeToDismissBoxValue.EndToStart -> SwipeDeleteColor // Color para borrar
                                    else -> Color.Transparent // Fondo transparente por defecto
                                }
                                val alignment = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                    SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                    else -> Alignment.Center
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(horizontal = 20.dp), // Padding para el texto del swipe
                                    contentAlignment = alignment
                                ) {
                                    Text(
                                        text = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> "Editar"
                                            SwipeToDismissBoxValue.EndToStart -> "Borrar"
                                            else -> ""
                                        },
                                        color = WhiteCard // Texto blanco sobre el fondo de color del swipe
                                    )
                                }
                            },
                            content = { // Contenido principal de la Card
                                ElevatedCard(
                                    onClick = {
                                        // La navegación para editar también puede ser activada por clic
                                        // O podrías tener una vista detallada aquí si el swipe es solo para acciones rápidas
                                        author.id?.let { navController.navigate("authors_form/${it}") }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = WhiteCard // Fondo de la card
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box( // Usamos Box para padding interno, puedes usar Column si necesitas más elementos
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp) // Padding interno de la card
                                    ) {
                                        Text(
                                            text = author.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = BlackText // Color del texto dentro de la card
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

    if (authorToDelete != null) {
        AlertDialog(
            containerColor = CreamBackground, // Fondo del diálogo
            titleContentColor = BlackText, // Color del título
            textContentColor = BlackText, // Color del texto
            onDismissRequest = { authorToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Seguro que quieres borrar a ${authorToDelete!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        authorToDelete?.let { deleteAuthor(it) }
                        authorToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { authorToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón
                ) {
                    Text("No")
                }
            }
        )
    }
}
