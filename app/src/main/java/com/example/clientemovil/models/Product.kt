package com.example.clientemovil.models


data class Product(
    val id: Int? = null,
    val title: String,
    val author: String,
    val publisher: String,
    val stock: Int,
    val price: Double
)
