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
// Nuevos modelos para las relaciones de Producto
data class ProductImage(
    val id: Int? = null,
    val url: String,
    val alt: String? = null,
    @SerializedName("is_main")
    val isMain: Boolean = false
)

data class SupplierCost(
    val id: Int? = null,
    @SerializedName("precio_proveedor")
    val supplierPrice: Double
)

/**
 * Modelo para representar un Producto.
 *
 * Utilizado tanto para el cuerpo de las peticiones (POST, PUT)
 * como para las respuestas del servidor (GET).
 */
data class Product(
    val id: Int? = null,
    val title: String,
    @SerializedName("publisher_id")
    val publisherId: Int,
    // El nombre de la editorial se incluye en la respuesta del servidor
    // pero no se envía en las peticiones.
    val publisher: String? = null,
    val stock: Int,
    val price: Double,
    // `preciodeproveedor` es opcional en la API
    @SerializedName("preciodeproveedor")
    val supplierPrice: Double? = null,
    // Las siguientes relaciones son arrays de objetos.
    // Pueden ser nulas si la API no los devuelve o si se omite en la petición.
    val authors: List<Author>? = null,
    val genres: List<Genre>? = null,
    val images: List<ProductImage>? = null,
    @SerializedName("supplierCost")
    val supplierCost: SupplierCost? = null
)

// NUEVO MODELO para las peticiones de CREACIÓN/ACTUALIZACIÓN
// Corresponde exactamente a lo que espera la API de Laravel
data class ProductRequest(
    val title: String,
    @SerializedName("publisher_id")
    val publisherId: Int,
    val stock: Int,
    val price: Double,
    @SerializedName("preciodeproveedor")
    val supplierPrice: Double? = null,
    @SerializedName("author_ids")
    val authorIds: Set<Int>? = null,
    @SerializedName("genre_ids")
    val genreIds: Set<Int>? = null,
    val images: List<ProductImage>? = null
)

data class ProductUpdateRequest(
    val id: Int,
    val title: String,
    @SerializedName("publisher_id")
    val publisherId: Int,
    val stock: Int?,
    val price: Double?,
    @SerializedName("preciodeproveedor")
    val supplierPrice: Double?,
    @SerializedName("author_ids")
    val authorIds: Set<Int>,
    @SerializedName("genre_ids")
    val genreIds: Set<Int>,
    val images: List<ProductImage>
)
