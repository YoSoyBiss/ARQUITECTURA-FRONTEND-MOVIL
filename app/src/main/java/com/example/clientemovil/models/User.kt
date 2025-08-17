package com.example.clientemovil.models

import com.google.gson.annotations.SerializedName
import java.util.Date



// Este modelo se usa en la API de ventas, donde el rol es un ID de tipo String.
data class User(
    @SerializedName("_id")
    val _id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val role: String? = null // <- Corregido: Es un String (el ID del rol)
)

// Este nuevo modelo se usa en la API de usuarios, donde el rol se devuelve como un objeto.
data class UserWithRoleObject(
    @SerializedName("_id")
    val _id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val role: Role? = null // <- Es un objeto de tipo Role
)

data class Role(
    @SerializedName("_id")
    val _id: String? = null,
    val name: String,
    val description: String? = null
)

data class Product(
    val id: Int? = null,
    val title: String,
    @SerializedName("publisher_id")
    val publisherId: Int,
    val stock: Int,
    val price: Double
)

data class SaleDetail(
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double
)


////////////////////////

// Este modelo sigue siendo útil para otras respuestas de la API de Node.js donde el rol es un objeto completo.
// Si no lo usas, podrías eliminarlo para evitar confusiones.
data class UserSingleResponse(
    @SerializedName("_id")
    val _id: String? = null,
    val name: String,
    val email: String,
    val password: String? = null,
    val role: Role? = null
)


// ... (El resto de tus modelos: Role, Product, SaleDetail, SaleRequest) ...




