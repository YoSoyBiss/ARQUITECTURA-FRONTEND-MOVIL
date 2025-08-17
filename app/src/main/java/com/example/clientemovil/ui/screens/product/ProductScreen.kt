package com.example.clientemovil.ui.screens.catalogos

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.models.Product
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(navController: NavController, canEdit: Boolean) {
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var isLoading by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<Product?>(null) }

    // Función para cargar productos desde la API
    val loadProducts = {
        coroutineScope.launch {
            isLoading = true
            try {
                val response = LaravelRetrofitClient.api.getAllProducts()
                if (response.isSuccessful) {
                    products = response.body() ?: emptyList()
                    Log.d("ProductsScreen", "Productos cargados: ${products.size}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("ProductsScreen", "Error al cargar productos. Código: ${response.code()}, Mensaje: $errorBody")
                    Toast.makeText(context, "Error al cargar productos: Código ${response.code()}", Toast.LENGTH_LONG).show()
                }
            } catch (e: IOException) {
                Log.e("ProductsScreen", "Error de red", e)
                Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
            } catch (e: HttpException) {
                Log.e("ProductsScreen", "Error HTTP", e)
                Toast.makeText(context, "Error HTTP: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Función para eliminar un producto
    val deleteProduct = { product: Product ->
        coroutineScope.launch {
            try {
                // El ID del producto puede ser nulo, por eso usamos `let`
                product.id?.let {
                    val response = LaravelRetrofitClient.api.deleteProduct(it)
                    if (response.isSuccessful) {
                        Toast.makeText(context, "Producto eliminado", Toast.LENGTH_SHORT).show()
                        loadProducts() // Recargar la lista después de la eliminación
                    } else {
                        Toast.makeText(context, "Error al eliminar", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error de red al eliminar", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        loadProducts()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Productos") }
            )
        },
        floatingActionButton = {
            // Mostrar FAB solo si el usuario tiene permisos de edición
            if (canEdit) {
                FloatingActionButton(onClick = {
                    // Navegar al formulario para crear un nuevo producto
                    navController.navigate("products_form/new")
                }) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar Producto")
                }
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
                    items(products, key = { it.id ?: 0 }) { product ->
                        if (canEdit) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { dismissValue ->
                                    when (dismissValue) {
                                        SwipeToDismissBoxValue.EndToStart -> {
                                            productToDelete = product
                                            false // No se descarta, se muestra el diálogo
                                        }
                                        SwipeToDismissBoxValue.StartToEnd -> {
                                            product.id?.let {
                                                // Navegar al formulario para editar
                                                navController.navigate("products_form/${it}")
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
                                    val icon = when (dismissState.targetValue) {
                                        SwipeToDismissBoxValue.StartToEnd -> Icons.Default.Edit
                                        SwipeToDismissBoxValue.EndToStart -> Icons.Default.Delete
                                        else -> null
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
                                        if (icon != null) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            ) {
                                Card(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    ProductCardContent(product)
                                }
                            }
                        } else {
                            // Si no se puede editar, solo mostrar la tarjeta sin swipe
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                ProductCardContent(product)
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo de confirmación de eliminación
    if (productToDelete != null) {
        AlertDialog(
            onDismissRequest = { productToDelete = null },
            title = { Text("Confirmar eliminación") },
            text = { Text("¿Estás seguro de que deseas eliminar el producto '${productToDelete?.title}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { deleteProduct(it) }
                        productToDelete = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                Button(
                    onClick = { productToDelete = null }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

/**
 * Composable que define el contenido de una tarjeta de producto.
 * Este Composable se extrajo para reducir la duplicación de código.
 */
@Composable
fun ProductCardContent(product: Product) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Título del producto
        Text(
            text = product.title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        // Precio y stock
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Precio: $${product.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Text(
                text = "Stock: ${product.stock}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        // Se corrige para acceder a las listas de autores y géneros
        // y mostrar los nombres correctamente.
        if (!product.authors.isNullOrEmpty()) {
            Text(
                text = "Autor(es): ${product.authors.joinToString(", ") { it.name }}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        if (!product.genres.isNullOrEmpty()) {
            Text(
                text = "Género(s): ${product.genres.joinToString(", ") { it.name }}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}
