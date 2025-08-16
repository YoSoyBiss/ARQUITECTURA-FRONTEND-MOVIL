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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.Screen
import com.example.clientemovil.models.Author
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

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
                    loadAuthors()
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
        topBar = {
            TopAppBar(
                title = { Text("Autores") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // CORRECCIÓN: Usar la ruta base y pasar el parámetro "new" directamente.
                navController.navigate("authors_form/new")
            }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Autor")
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
                CircularProgressIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(authors, key = { it.id ?: 0 }) { author ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                when (dismissValue) {
                                    SwipeToDismissBoxValue.EndToStart -> {
                                        authorToDelete = author
                                        false
                                    }
                                    SwipeToDismissBoxValue.StartToEnd -> {
                                        author.id?.let {
                                            // CORRECCIÓN: Pasar el ID como un argumento de la ruta
                                            navController.navigate("authors_form/${it}")
                                        }
                                        false
                                    }
                                    else -> false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            backgroundContent = {
                                val color = when (dismissState.targetValue) {
                                    SwipeToDismissBoxValue.StartToEnd -> Color.Blue
                                    SwipeToDismissBoxValue.EndToStart -> Color.Red
                                    else -> Color.Transparent
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(color)
                                        .padding(16.dp),
                                    contentAlignment = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                        else -> Alignment.Center
                                    }
                                ) {
                                    Text(
                                        text = when (dismissState.targetValue) {
                                            SwipeToDismissBoxValue.StartToEnd -> "Editar"
                                            SwipeToDismissBoxValue.EndToStart -> "Borrar"
                                            else -> ""
                                        },
                                        color = Color.White
                                    )
                                }
                            },
                            content = {
                                ElevatedCard(
                                    onClick = { author.id?.let { navController.navigate("authors_form/${it}") } },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = author.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Bold
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
            onDismissRequest = { authorToDelete = null },
            title = { Text("Confirmar borrado") },
            text = { Text("¿Seguro que quieres borrar a ${authorToDelete!!.name}?") },
            confirmButton = {
                TextButton(onClick = {
                    authorToDelete?.let { deleteAuthor(it) }
                    authorToDelete = null
                }) {
                    Text("Sí")
                }
            },
            dismissButton = {
                TextButton(onClick = { authorToDelete = null }) {
                    Text("No")
                }
            }
        )
    }
}
