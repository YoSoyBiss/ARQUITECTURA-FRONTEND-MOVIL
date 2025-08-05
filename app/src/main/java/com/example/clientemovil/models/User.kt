package com.example.clientemovil.models

data class User(
    val _id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val role: String
)