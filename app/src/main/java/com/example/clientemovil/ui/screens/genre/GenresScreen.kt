package com.example.clientemovil.ui.screens.catalogos // Path actualizado

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
// import com.example.clientemovil.Screen // No se usa en este archivo
import com.example.clientemovil.models.Genre
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
// Colores para las acciones de swipe (puedes definirlos en tu Color.kt o usar genéricos)
val SwipeEditColorGenres = Color.Blue // Ejemplo, cámbialo por tu color de tema deseado
val SwipeDeleteColorGenres = Color.Red   // Ejemplo, cámbialo por tu color de tema deseado


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenresScreen(navController: NavController) {
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var genreToDelete by remember { mutableStateOf<Genre?>(null) }

    val loadGenres = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getAllGenres()
                if (response.isSuccessful) {
                    genres = response.body() ?: emptyList()
                } else {
                    Toast.makeText(context, "Error al cargar géneros", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_SHORT).show()
            } catch (e: HttpException) {
                Toast.makeText(context, "Error HTTP: ${e.message}", Toast.LENGTH_SHORT).show()
            } finally {
                isLoading = false
            }
        }
    }

    val deleteGenre = { genre: Genre ->
        coroutineScope.launch {
            try {
                val response = LaravelRetrofitClient.api.deleteGenre(genre.id!!)
                if (response.isSuccessful) {
                    Toast.makeText(context, "Género eliminado", Toast.LENGTH_SHORT).show()
                    loadGenres() // Recarga la lista
                } else {
                    Toast.makeText(context, "Error al eliminar género", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadGenres()
    }

    Scaffold(
        containerColor = CreamBackground, // Color de fondo principal
        topBar = {
            TopAppBar(
                title = { Text("Géneros", color = WhiteCard) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BrownBorder, // Color de fondo de la TopAppBar
                    titleContentColor = WhiteCard // Color del título
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("genres_form/new") },
                containerColor = BrownBorder, // Color de fondo del FAB
                contentColor = WhiteCard // Color del icono dentro del FAB
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Género")
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
                    items(genres, key = { it.id ?: 0 }) { genre ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> { // Swipe para borrar
                                        genreToDelete = genre
                                        true // Permite que el swipe complete la acción (mostrar diálogo)
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> { // Swipe para editar
                                        genre.id?.let {
                                            navController.navigate("genres_form/${it}")
                                        }
                                        true // Permite que el swipe complete la acción (navegar)
                                    }
                                    else -> false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> SwipeEditColorGenres
                                    SwipeToDismissBoxValue.EndToStart -> SwipeDeleteColorGenres
                                    else -> Color.Transparent
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
                                        // Navegación para editar también activada por clic
                                        genre.id?.let { navController.navigate("genres_form/${it}") }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.elevatedCardColors(
                                        containerColor = WhiteCard // Fondo de la card
                                    ),
                                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp) // Padding interno de la card
                                    ) {
                                        Text(
                                            text = genre.name,
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

    if (genreToDelete != null) {
        AlertDialog(
            containerColor = CreamBackground, // Fondo del diálogo
            titleContentColor = BlackText, // Color del título
            textContentColor = BlackText, // Color del texto
            onDismissRequest = { genreToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Seguro que quieres borrar a ${genreToDelete!!.name}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        genreToDelete?.let { deleteGenre(it) }
                        genreToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón
                ) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { genreToDelete = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = BrownBorder) // Color del texto del botón
                ) {
                    Text("No")
                }
            }
        )
    }
}
