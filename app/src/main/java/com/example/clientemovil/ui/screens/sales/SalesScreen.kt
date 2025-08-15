package com.example.clientemovil.ui.screens.sales

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.clientemovil.Screen
import com.example.clientemovil.models.SaleResponse
import com.example.clientemovil.network.node.NodeRetrofitClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(navController: NavController) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var sales by remember { mutableStateOf<List<SaleResponse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    suspend fun loadSales() {
        isLoading = true
        try {
            val response = NodeRetrofitClient.api.getAllSales()
            if (response.isSuccessful) {
                sales = response.body() ?: emptyList()
            } else {
                Toast.makeText(context, "Error al cargar ventas: ${response.code()}", Toast.LENGTH_SHORT).show()
                Log.e("SalesScreen", "Error al cargar ventas: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Excepción: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("SalesScreen", "Error al cargar ventas", e)
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        scope.launch {
            loadSales()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Gestión de Ventas") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(Screen.SaleForm.route.replace("{saleId}", "create")) }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Venta")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                sales.isEmpty() -> {
                    Text("No hay ventas", Modifier.align(Alignment.Center))
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(sales, key = { it._id ?: it.total.toString() }) { sale ->
                            ElevatedCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth()
                                ) {
                                    Text(text = "Total: $${String.format("%.2f", sale.total)}", style = MaterialTheme.typography.titleMedium)
                                    Text(text = "Cliente: ${sale.userId?.name ?: "N/A"}", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "Fecha: ${sale.date?.let { formatter.format(it) } ?: "N/A"}", style = MaterialTheme.typography.bodySmall)
                                    sale.details?.let { details ->
                                        Text("Productos:", style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 8.dp))
                                        details.forEach { detail ->
                                            Text("  - Producto ID: ${detail.productId}, Cantidad: ${detail.quantity}", style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}