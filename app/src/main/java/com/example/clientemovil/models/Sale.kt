package com.example.clientemovil.models

data class Sale(
    val _id: String? = null,
    val total: Double,
    val userId: String,
    val details: List<SaleDetail>
)

data class SaleDetail(
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double
)

