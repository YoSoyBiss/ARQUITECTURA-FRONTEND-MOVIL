package com.example.clientemovil.ui.screens.sales

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Product
import com.example.clientemovil.models.SaleDetailRequestItem
import com.example.clientemovil.models.SaleRequest
import com.example.clientemovil.models.UserWithRoleObject
import com.example.clientemovil.network.node.NodeRetrofitClient
import com.example.clientemovil.network.laravel.LaravelRetrofitClient
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.example.clientemovil.models.CreateSaleSuccessResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesFormScreen(
    saleId: String? = null,
    onBack: () -> Unit = {}
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = saleId != null && saleId != "create"

    var selectedUser by remember { mutableStateOf<UserWithRoleObject?>(null) }
    var availableUsers by remember { mutableStateOf<List<UserWithRoleObject>>(emptyList()) }
    var availableProducts by remember { mutableStateOf<List<Product>>(emptyList()) }
    var saleDetails by remember { mutableStateOf<List<SaleDetailRequestItem>>(emptyList()) }
    var totalSalePrice by remember { mutableStateOf(0.0) }

    var isLoading by remember { mutableStateOf(false) }

    // Estados para el diálogo de error
    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Estados para el diálogo de cantidad
    var showQuantityDialog by remember { mutableStateOf(false) }
    var selectedProductToAdd by remember { mutableStateOf<Product?>(null) }
    var quantityInput by remember { mutableStateOf("1") }

    suspend fun loadUsers() {
        try {
            val response = NodeRetrofitClient.api.getAllUsers()
            if (response.isSuccessful) {
                availableUsers = response.body() ?: emptyList()
            } else {
                Log.e("SalesFormScreen", "Error al cargar usuarios: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SalesFormScreen", "Excepción al cargar usuarios", e)
        }
    }

    suspend fun loadProducts() {
        isLoading = true
        try {
            val response = LaravelRetrofitClient.api.getAllProducts()
            if (response.isSuccessful) {
                availableProducts = response.body() ?: emptyList()
            } else {
                Log.e("SalesFormScreen", "Error al cargar productos: ${response.code()}")
            }
        } catch (e: Exception) {
            Log.e("SalesFormScreen", "Excepción al cargar productos", e)
        } finally {
            isLoading = false
        }
    }

    fun addProductToCart(product: Product, quantity: Int) {
        val existingDetail = saleDetails.find { it.productId == product.id }
        if (existingDetail != null) {
            saleDetails = saleDetails.map {
                if (it.productId == product.id) {
                    it.copy(quantity = it.quantity + quantity)
                } else {
                    it
                }
            }
        } else {
            saleDetails = saleDetails + SaleDetailRequestItem(
                productId = product.id!!,
                quantity = quantity
            )
        }
        // Recalcular el total
        totalSalePrice = saleDetails.sumOf { detail ->
            val productPrice = availableProducts.find { it.id == detail.productId }?.price ?: 0.0
            productPrice * detail.quantity
        }
    }

    // Función para manejar el clic en el botón de "Añadir"
    fun onAddProductClicked(product: Product) {
        selectedProductToAdd = product
        quantityInput = "1"
        showQuantityDialog = true
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadUsers()
            loadProducts()
        }
    }

    // Diálogo de cantidad de producto
    if (showQuantityDialog && selectedProductToAdd != null) {
        AlertDialog(
            onDismissRequest = { showQuantityDialog = false },
            title = { Text(text = "Añadir ${selectedProductToAdd!!.title}") },
            text = {
                Column {
                    Text("Stock disponible: ${selectedProductToAdd!!.stock}")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { newValue ->
                            quantityInput = newValue.filter { it.isDigit() }
                        },
                        label = { Text("Cantidad") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val quantity = quantityInput.toIntOrNull() ?: 0
                        if (quantity > 0 && quantity <= selectedProductToAdd!!.stock) {
                            addProductToCart(selectedProductToAdd!!, quantity)
                            showQuantityDialog = false
                        } else {
                            Toast.makeText(context, "La cantidad debe ser mayor que 0 y menor o igual al stock disponible.", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Añadir")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQuantityDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Diálogo de error
    if (showErrorDialog && errorMessage != null) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text(text = "Error en la Venta") },
            text = { Text(text = errorMessage!!) },
            confirmButton = {
                Button(onClick = { showErrorDialog = false }) {
                    Text("Aceptar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Crear Venta") },
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
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    var userExpanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = userExpanded,
                        onExpandedChange = { userExpanded = !userExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            readOnly = true,
                            value = selectedUser?.name ?: "Seleccionar Cliente",
                            onValueChange = {},
                            label = { Text("Cliente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = userExpanded) }
                        )
                        ExposedDropdownMenu(
                            expanded = userExpanded,
                            onDismissRequest = { userExpanded = false }
                        ) {
                            availableUsers.forEach { userOption ->
                                DropdownMenuItem(
                                    text = { Text(userOption.name) },
                                    onClick = {
                                        selectedUser = userOption
                                        userExpanded = false
                                    },
                                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                )
                            }
                        }
                    }

                    Text("Selecciona Productos", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(availableProducts, key = { it.id ?: it.title }) { product ->
                            ProductItem(product) {
                                onAddProductClicked(product)
                            }
                        }
                    }

                    Text("Productos en el carrito:", style = MaterialTheme.typography.titleSmall)
                    saleDetails.forEach { detail ->
                        val product = availableProducts.find { it.id == detail.productId }
                        if (product != null) {
                            Text("- ${product.title} (x${detail.quantity})", style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    Text("Total de la venta: $${String.format("%.2f", totalSalePrice)}", style = MaterialTheme.typography.titleLarge)

                    Button(
                        onClick = {
                            scope.launch {
                                if (selectedUser == null) {
                                    Toast.makeText(context, "Por favor, seleccione un cliente", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                if (saleDetails.isEmpty()) {
                                    Toast.makeText(context, "Por favor, añada productos a la venta", Toast.LENGTH_SHORT).show()
                                    return@launch
                                }
                                try {
                                    val response = NodeRetrofitClient.api.createSale(
                                        SaleRequest(
                                            userId = selectedUser!!._id!!,
                                            details = saleDetails
                                        )
                                    )
                                    if (response.isSuccessful) {
                                        Toast.makeText(context, "Venta creada con éxito", Toast.LENGTH_SHORT).show()
                                        onBack()
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        if (errorBody != null) {
                                            try {
                                                val jsonError = JSONObject(errorBody)
                                                val msg = jsonError.getString("error")
                                                errorMessage = msg
                                                showErrorDialog = true
                                                Log.e("SalesFormScreen", "Error al crear venta: $msg")
                                            } catch (e: Exception) {
                                                errorMessage = "Error al crear venta: ${response.code()}"
                                                showErrorDialog = true
                                                Log.e("SalesFormScreen", "Error al crear venta: ${response.code()}, Cuerpo del error: $errorBody", e)
                                            }
                                        } else {
                                            errorMessage = "Error al crear venta: ${response.code()}"
                                            showErrorDialog = true
                                            Log.e("SalesFormScreen", "Error al crear venta: ${response.code()}")
                                        }
                                    }
                                } catch (e: Exception) {
                                    errorMessage = "Excepción: ${e.message}"
                                    showErrorDialog = true
                                    Log.e("SalesFormScreen", "Excepción al crear venta", e)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedUser != null && saleDetails.isNotEmpty()
                    ) {
                        Text(text = "Crear Venta")
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onAdd: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(product.title, style = MaterialTheme.typography.titleMedium)
                Text("Precio: $${String.format("%.2f", product.price)}", style = MaterialTheme.typography.bodyMedium)
                Text("Stock: ${product.stock}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Default.Add, contentDescription = "Añadir producto")
            }
        }
    }
}
