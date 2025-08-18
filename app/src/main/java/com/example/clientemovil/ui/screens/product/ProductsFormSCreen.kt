package com.example.clientemovil.ui.screens.catalogos // O el paquete correcto de tu archivo

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
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
// Importa tus colores personalizados
import com.example.clientemovil.ui.theme.BlackText
import com.example.clientemovil.ui.theme.BrownBorder
import com.example.clientemovil.ui.theme.CreamBackground
import com.example.clientemovil.ui.theme.WhiteCard
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

    // Define textFieldColors aquí, dentro del contexto composable de ProductsFormScreen
    val textFieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = BrownBorder,
        unfocusedBorderColor = BrownBorder.copy(alpha = 0.7f),
        focusedLabelColor = BrownBorder,
        unfocusedLabelColor = BlackText.copy(alpha = 0.7f), // O BrownBorder.copy(alpha = 0.7f)
        cursorColor = BrownBorder,
        focusedTextColor = BlackText,
        unfocusedTextColor = BlackText,
        // Estos son clave para el color del TrailingIcon en ExposedDropdownMenu
        // si el parámetro 'tint' directo no está disponible o no se usa.
        focusedTrailingIconColor = BrownBorder,
        unfocusedTrailingIconColor = BrownBorder.copy(alpha = 0.7f)
    )

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
            } catch (e: Exception) {
                Log.e(TAG, "Error al cargar catálogos: ${e.message}")
                Toast.makeText(context, "Error al cargar catálogos: ${e.message}", Toast.LENGTH_LONG).show()
            } finally {
                isLoading = false
            }
        }
    }

    // Cargar datos del producto existente
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
        containerColor = CreamBackground,
        topBar = {
            TopAppBar(
                title = { Text(if (isEditing) "Editar Producto" else "Nuevo Producto", color = WhiteCard) },
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
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = BrownBorder)
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = stock,
                            onValueChange = { stock = it },
                            label = { Text("Stock") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = price,
                            onValueChange = { price = it },
                            label = { Text("Precio") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                    }

                    item {
                        OutlinedTextField(
                            value = supplierPrice,
                            onValueChange = { supplierPrice = it },
                            label = { Text("Precio de Proveedor (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = textFieldColors
                        )
                    }

                    item {
                        CustomDropdownMenu(
                            label = "Editorial",
                            items = publishers,
                            selectedItem = selectedPublisher,
                            onItemSelected = { selectedPublisher = it },
                            nameExtractor = { it.name },
                            textFieldColors = textFieldColors // Pasa la instancia de colores
                        )
                    }

                    item {
                        Text("Autores", style = MaterialTheme.typography.titleMedium, color = BlackText)
                        MultiSelectGrid(
                            items = authors,
                            selectedItemIds = selectedAuthorIds,
                            onSelectionChanged = { selectedAuthorIds = it },
                            idExtractor = { it.id },
                            nameExtractor = { it.name }
                        )
                    }

                    item {
                        Text("Géneros", style = MaterialTheme.typography.titleMedium, color = BlackText)
                        MultiSelectGrid(
                            items = genres,
                            selectedItemIds = selectedGenreIds,
                            onSelectionChanged = { selectedGenreIds = it },
                            idExtractor = { it.id },
                            nameExtractor = { it.name }
                        )
                    }

                    item {
                        Text("Imágenes", style = MaterialTheme.typography.titleMedium, color = BlackText)
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
                                    modifier = Modifier.weight(1f),
                                    colors = textFieldColors
                                )
                                IconButton(
                                    onClick = { images = images.toMutableList().apply { removeAt(index) } },
                                    colors = IconButtonDefaults.iconButtonColors(contentColor = BrownBorder)
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Eliminar imagen")
                                }
                            }
                        }
                        Button(
                            onClick = { images = images + "" }, // Añade una URL vacía para un nuevo campo
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrownBorder,
                                contentColor = WhiteCard
                            )
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
                                                    val response: Response<out Any> = if (isEditing) {
                                                        val productToUpdate = ProductUpdateRequest(
                                                            id = productId!!,
                                                            title = title,
                                                            publisherId = publisherId,
                                                            stock = stock.toIntOrNull() ?: 0,
                                                            price = priceValue,
                                                            supplierPrice = supplierPriceValue,
                                                            authorIds = selectedAuthorIds,
                                                            genreIds = selectedGenreIds,
                                                            images = images.filter { it.isNotBlank() }.map { ProductImage(url = it) } // Filtrar vacías
                                                        )
                                                        LaravelRetrofitClient.api.updateProduct(productId, productToUpdate)
                                                    } else {
                                                        val productToCreate = ProductRequest(
                                                            title = title,
                                                            publisherId = publisherId,
                                                            stock = stock.toIntOrNull() ?: 0,
                                                            price = priceValue,
                                                            supplierPrice = supplierPriceValue,
                                                            authorIds = selectedAuthorIds,
                                                            genreIds = selectedGenreIds,
                                                            images = images.filter { it.isNotBlank() }.map { ProductImage(url = it) } // Filtrar vacías
                                                        )
                                                        LaravelRetrofitClient.api.createProduct(productToCreate)
                                                    }
                                                    if (response.isSuccessful) {
                                                        Toast.makeText(context, "Producto ${if (isEditing) "actualizado" else "creado"}", Toast.LENGTH_SHORT).show()
                                                        onBack()
                                                    } else {
                                                        val errorBody = response.errorBody()?.string() ?: "Error desconocido"
                                                        Toast.makeText(context, "Error: $errorBody", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "ID de editorial no válido.", Toast.LENGTH_LONG).show()
                                                }
                                            } ?: run {
                                                Toast.makeText(context, "Por favor, selecciona una editorial.", Toast.LENGTH_LONG).show()
                                            }
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Error de red: ${e.message}", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = title.isNotBlank() && stock.isNotBlank() && price.isNotBlank() && selectedPublisher != null,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = BrownBorder,
                                    contentColor = WhiteCard,
                                    disabledContainerColor = BrownBorder.copy(alpha = 0.5f),
                                    disabledContentColor = WhiteCard.copy(alpha = 0.7f)
                                )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> CustomDropdownMenu(
    label: String,
    items: List<T>,
    selectedItem: T?,
    onItemSelected: (T) -> Unit,
    nameExtractor: (T) -> String = { it.toString() },
    textFieldColors: TextFieldColors // Recibe la instancia de colores
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
            label = { Text(label) }, // El color de la etiqueta ya se define en textFieldColors
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(), // Importante para que el menú se ancle correctamente
            colors = textFieldColors // Usa los colores proporcionados
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(WhiteCard) // Fondo del menú desplegable
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(nameExtractor(item), color = BlackText) },
                    onClick = {
                        onItemSelected(item)
                        expanded = false
                    },
                    colors = MenuDefaults.itemColors( // Colores para los items del menú
                        textColor = BlackText
                    )
                )
            }
        }
    }
}

@Composable
fun <T> MultiSelectGrid(
    items: List<T>,
    selectedItemIds: Set<Int>,
    onSelectionChanged: (Set<Int>) -> Unit,
    idExtractor: (T) -> Int?, // Asegúrate que T tenga una forma de extraer un Int ID
    nameExtractor: (T) -> String = { it.toString() }
) {
    Column {
        items.forEach { item ->
            val id = idExtractor(item)
            // Solo procesa si el ID no es nulo, para seguridad
            if (id != null) {
                val isSelected = selectedItemIds.contains(id)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        // .clickable { // Opcional: permitir clic en toda la fila para seleccionar/deseleccionar
                        // val newSelection = if (isSelected) selectedItemIds - id else selectedItemIds + id
                        // onSelectionChanged(newSelection)
                        // }
                        .padding(vertical = 4.dp)
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { checked ->
                            val newSelection = if (checked) {
                                selectedItemIds + id
                            } else {
                                selectedItemIds - id
                            }
                            onSelectionChanged(newSelection)
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = BrownBorder,
                            uncheckedColor = BrownBorder.copy(alpha = 0.7f), // Color del borde cuando no está seleccionado
                            checkmarkColor = WhiteCard
                        )
                    )
                    Spacer(Modifier.width(8.dp)) // Espacio entre checkbox y texto
                    Text(text = nameExtractor(item), color = BlackText)
                }
            }
        }
    }
}
