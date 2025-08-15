package com.example.clientemovil.ui.screens.sales

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.clientemovil.models.Product
import com.example.clientemovil.models.SaleRequest
import com.example.clientemovil.models.UserWithRoleObject
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesFormScreen(
    saleId: String? = null,
    onBack: () -> Unit = {} // <-- ¡Aquí está el nuevo parámetro!
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isEditing = saleId != null && saleId != "create"

    var selectedUser by remember { mutableStateOf<UserWithRoleObject?>(null) }
    var availableUsers by remember { mutableStateOf<List<UserWithRoleObject>>(emptyList()) }
    var availableProducts by remember { mutableStateOf<List<Product>>(emptyList()) }

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
        // ... (Tu código para cargar productos)
    }

    suspend fun loadSale(id: String) {
        // ... (Tu código para cargar una venta existente)
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadUsers()
            loadProducts()
            if (isEditing && saleId != null) {
                loadSale(saleId)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Crear Venta") },
                navigationIcon = {
                    IconButton(onClick = onBack) { // <-- Se utiliza el nuevo parámetro onBack
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
                    // Dropdown para seleccionar usuario
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

                    // Botón para crear venta
                    Button(
                        onClick = {
                            scope.launch {
                                selectedUser?.let { user ->
                                    val saleRequest = SaleRequest(
                                        userId = user._id!!,
                                        details = emptyList()
                                    )
                                    createSale(saleRequest, context)
                                    onBack() // <-- Se llama a onBack después de crear la venta
                                } ?: Toast.makeText(context, "Por favor, seleccione un cliente", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedUser != null
                    ) {
                        Text(text = "Crear Venta")
                    }
                }
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