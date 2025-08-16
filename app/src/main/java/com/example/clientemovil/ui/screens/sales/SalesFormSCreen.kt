package com.example.clientemovil.ui.screens.sales

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Product
import com.example.clientemovil.models.SaleDetail
import com.example.clientemovil.models.SaleRequest
import com.example.clientemovil.models.UserWithRoleObject
import com.example.clientemovil.network.node.NodeRetrofitClient
import com.example.clientemovil.network.laravel.LaravelRetrofitClient // <-- NUEVA IMPORTACIÓN
import kotlinx.coroutines.launch

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
    var saleDetails by remember { mutableStateOf<List<SaleDetail>>(emptyList()) }

    var isLoading by remember { mutableStateOf(false) }

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
            val response = LaravelRetrofitClient.api.getAllProducts() // <-- CAMBIO AQUÍ
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

    fun addProductToCart(product: Product) {
        val existingDetail = saleDetails.find { it.productId == product.id }
        if (existingDetail != null) {
            saleDetails = saleDetails.map {
                if (it.productId == product.id) {
                    it.copy(quantity = it.quantity + 1)
                } else {
                    it
                }
            }
        } else {
            saleDetails = saleDetails + SaleDetail(
                productId = product.id!!,
                quantity = 1,
                unitPrice = product.price
            )
        }
        Toast.makeText(context, "${product.title} añadido", Toast.LENGTH_SHORT).show()
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadUsers()
            loadProducts()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Crear Venta") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                                addProductToCart(product)
                            }
                        }
                    }

                    val total = saleDetails.sumOf { it.quantity * it.unitPrice }
                    Text("Total de la venta: $${String.format("%.2f", total)}", style = MaterialTheme.typography.titleLarge)

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
                                val saleRequest = SaleRequest(
                                    userId = selectedUser!!._id!!,
                                    details = saleDetails
                                )
                                createSale(saleRequest, context)
                                onBack()
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

private suspend fun createSale(saleRequest: SaleRequest, context: android.content.Context) {
    try {
        val response = NodeRetrofitClient.api.createSale(saleRequest)
        if (response.isSuccessful) {
            Toast.makeText(context, "Venta creada con éxito", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Error al crear venta: ${response.code()}", Toast.LENGTH_SHORT).show()
            Log.e("SalesFormScreen", "Error al crear venta: ${response.errorBody()?.string()}")
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
        Log.e("SalesFormScreen", "Excepción al crear venta", e)
    }
}
