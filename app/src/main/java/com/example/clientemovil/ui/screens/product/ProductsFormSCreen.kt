package com.example.clientemovil.ui.screens.catalogos

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Author
import com.example.clientemovil.models.Genre
import com.example.clientemovil.models.ProductImage
import com.example.clientemovil.models.ProductRequest
import com.example.clientemovil.models.ProductUpdateRequest
import com.example.clientemovil.models.Publisher
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch
import retrofit2.Response

// Tag para el Logcat
private const val TAG = "ProductsFormScreen"
// Límite de precio para coincidir con DECIMAL(8, 2) en la base de datos
private const val MAX_PRICE = 999999.99

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsFormScreen(
    productId: Int?,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = productId != null
    var isLoading by remember { mutableStateOf(false) }

    // Estado del formulario
    var title by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var supplierPrice by remember { mutableStateOf("") }
    var selectedPublisher by remember { mutableStateOf<Publisher?>(null) }
    var selectedAuthorIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var selectedGenreIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
    var images by remember { mutableStateOf<List<String>>(emptyList()) }

    // Listas de datos para los selectores
    var publishers by remember { mutableStateOf<List<Publisher>>(emptyList()) }
    var authors by remember { mutableStateOf<List<Author>>(emptyList()) }
    var genres by remember { mutableStateOf<List<Genre>>(emptyList()) }

    // Carga de datos iniciales para los selectores
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            try {
                isLoading = true
                val publishersResponse = LaravelRetrofitClient.api.getAllPublishers().body() ?: emptyList()
                val authorsResponse = LaravelRetrofitClient.api.getAllAuthors().body() ?: emptyList()
                val genresResponse = LaravelRetrofitClient.api.getAllGenres().body() ?: emptyList()

                publishers = publishersResponse
                authors = authorsResponse
                genres = genresResponse
                Log.d(TAG, "Catálogos cargados. Autores: ${authors.size}, Géneros: ${genres.size}")
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar catálogos: ${e.message}")
                Toast.makeText(context, "Error al cargar catálogos: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar datos del producto existente cuando las listas de catálogos están listas
    LaunchedEffect(isEditing, productId, authors, genres) {
        if (isEditing && productId != null && authors.isNotEmpty() && genres.isNotEmpty()) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val productResponse = LaravelRetrofitClient.api.getProduct(productId)
                    if (productResponse.isSuccessful) {
                        val product = productResponse.body()!!
                        title = product.title
                        stock = product.stock.toString()
                        price = product.price.toString()
                        supplierPrice = product.supplierPrice?.toString() ?: ""
                        selectedPublisher = publishers.find { it.id == product.publisherId }

                        selectedAuthorIds = product.authors?.mapNotNull { it.id }?.toSet() ?: emptySet()
                        selectedGenreIds = product.genres?.mapNotNull { it.id }?.toSet() ?: emptySet()

                        images = product.images?.map { it.url } ?: emptyList()

                        Log.d(TAG, "Producto cargado. Título: ${product.title}")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error al cargar datos del producto: ${e.message}")
                    Toast.makeText(context, "Error al cargar datos del producto: ${e.message}", Toast.LENGTH_LONG).show()
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Producto" else "Nuevo Producto") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver") } }
            )
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
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Título") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Precio") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = supplierPrice,
                            onValueChange = { supplierPrice = it },
                            label = { Text("Precio de Proveedor (opcional)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    item {
                        DropdownMenu(
                            label = "Editorial",
                            items = publishers,
                            selectedItem = selectedPublisher,
                            onItemSelected = { selectedPublisher = it },
                            nameExtractor = { it.name }
                        )
                    }

                    item {
                        Text("Autores", style = MaterialTheme.typography.titleMedium)
                        MultiSelectGrid(
                            items = authors,
                            selectedItemIds = selectedAuthorIds,
                            onSelectionChanged = { selectedAuthorIds = it },
                            idExtractor = { it.id },
                            nameExtractor = { it.name }
                        )
                    }

                    item {
                        Text("Géneros", style = MaterialTheme.typography.titleMedium)
                        MultiSelectGrid(
                            items = genres,
                            selectedItemIds = selectedGenreIds,
                            onSelectionChanged = { selectedGenreIds = it },
                            idExtractor = { it.id },
                            nameExtractor = { it.name }
                        )
                    }

                    item {
                        Text("Imágenes", style = MaterialTheme.typography.titleMedium)
                        images.forEachIndexed { index, url ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = url,
                                    onValueChange = {
                                        images = images.toMutableList().apply { this[index] = it }
                                    },
                                    label = { Text("URL de la imagen ${index + 1}") },
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { images = images.toMutableList().apply { removeAt(index) } }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar imagen")
                                }
                            }
                        }
                        Button(
                            onClick = { images = images + "" },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar imagen")
                            Spacer(Modifier.width(8.dp))
                            Text("Agregar imagen")
                        }
                    }

                    item {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        val priceValue = price.toDoubleOrNull()
                                        val supplierPriceValue = supplierPrice.toDoubleOrNull()

                                        if (priceValue == null || priceValue > MAX_PRICE) {
                                            Toast.makeText(context, "El precio es inválido o excede el máximo permitido ($MAX_PRICE)", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }

                                        if (supplierPriceValue != null && supplierPriceValue > MAX_PRICE) {
                                            Toast.makeText(context, "El precio de proveedor excede el máximo permitido ($MAX_PRICE)", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }

                                        try {
                                            selectedPublisher?.let { publisher ->
                                                val publisherId = publisher.id
                                                if (publisherId != null) {
                                                    val response = if (isEditing) {
                                                        // Usar el nuevo modelo para la actualización
                                                        val productToUpdate = ProductUpdateRequest(
                                                            id = productId!!,
                                                            title = title,
                                                            publisherId = publisherId,
                                                            stock = stock.toIntOrNull() ?: 0,
                                                            price = priceValue,
                                                            supplierPrice = supplierPriceValue,
                                                            authorIds = selectedAuthorIds,
                                                            genreIds = selectedGenreIds,
                                                            images = images.map { ProductImage(url = it) }
                                                        )
                                                        LaravelRetrofitClient.api.updateProduct(productId, productToUpdate)
                                                    } else {
                                                        // La creación sigue usando el modelo ProductRequest
                                                        val productToCreate = ProductRequest(
                                                            title = title,
                                                            publisherId = publisherId,
                                                            stock = stock.toIntOrNull() ?: 0,
                                                            price = priceValue,
                                                            supplierPrice = supplierPriceValue,
                                                            authorIds = selectedAuthorIds,
                                                            genreIds = selectedGenreIds,
                                                            images = images.map { ProductImage(url = it) }
                                                        )
                                                        LaravelRetrofitClient.api.createProduct(productToCreate)
                                                    }
                                                    if (response.isSuccessful) {
                                                        Log.d(TAG, "Producto creado/actualizado correctamente.")
                                                        Toast.makeText(context, "Producto ${if (isEditing) "actualizado" else "creado"}", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    } else {
                                                        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                                                        Log.e(TAG, "Error del servidor: $errorBody")
                                                        Toast.makeText(context, "Error: $errorBody", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Log.e(TAG, "ID de editorial nulo. No se puede crear/actualizar el producto.")
                                                    Toast.makeText(context, "ID de editorial no válido.", Toast.LENGTH_LONG).show()
                                                }
                                            } ?: run {
                                                Log.e(TAG, "Editorial no seleccionada, no se puede crear/actualizar el producto.")
                                                Toast.makeText(context, "Por favor, selecciona una editorial.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Log.e(TAG, "Error de red: ${e.message}", e)
                                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = title.isNotBlank() && stock.isNotBlank() && price.isNotBlank() && selectedPublisher != null
                            ) {
                                Text(if (isEditing) "Guardar Cambios" else "Crear Producto")
                            }
                        }
                    }
                }
            }
        }
    }
}

// Componente reutilizable para el DropdownMenu
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenu(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    nameExtractor: (T) -> String = { it.toString() }
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedItem?.let { nameExtractor(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(nameExtractor(item)) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Componente para la selección múltiple
@Composable
fun <T> MultiSelectGrid(
    items: List<T>,
    selectedItemIds: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    idExtractor: (T) -> Int?,
    nameExtractor: (T) -> String = { it.toString() }
) {
    Column {
        items.forEach { item ->
            val id = idExtractor(item)
            val isSelected = id != null && selectedItemIds.contains(id)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { checked ->
                        if (id != null) {
                            val newSelection = if (checked) {
                                selectedItemIds + id
                            } else {
                                selectedItemIds - id
                            }
                            onSelectionChanged(newSelection)
                        }
                    }
                )
                Text(text = nameExtractor(item))
            }
        }
    }
}
