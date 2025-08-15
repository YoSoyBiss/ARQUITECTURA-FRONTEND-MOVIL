package com.example.clientemovil.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

sealed class ListItem {
    data class ProductItem(val id: Int?, val title: String, val author: String, val price: Double) : ListItem()
    data class UserItem(val id: String?, val name: String, val email: String, val role: String) : ListItem()
    data class SaleItem(val id: String?, val total: Double, val userId: String) : ListItem()
}

@Composable
fun GenericListing(
    items: List<ListItem>,
    title: String
) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(text = title, style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn {
            items(items) { item ->
                when (item) {
                    is ListItem.ProductItem -> {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("📚 ${item.title}", style = MaterialTheme.typography.titleMedium)
                                Text("✍️ ${item.author}")
                                Text("💲${item.price}")
                            }
                        }
                    }

                    is ListItem.UserItem -> {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("👤 ${item.name}", style = MaterialTheme.typography.titleMedium)
                                Text("📧 ${item.email}")
                                Text("🔐 ${item.role}")
                            }
                        }
                    }

                    is ListItem.SaleItem -> {
                        ElevatedCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🧾 Venta: ${item.id ?: "N/A"}", style = MaterialTheme.typography.titleMedium)
                                Text("👤 Usuario: ${item.userId}")
                                Text("💰 Total: ${item.total}")
                            }
                        }
                    }
                }
            }
        }
    }
}
