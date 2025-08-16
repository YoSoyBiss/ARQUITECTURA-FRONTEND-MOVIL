package com.example.clientemovil.models


import com.google.gson.annotations.SerializedName

/**
 * Modelo de datos para representar un Autor.
 */
data class Author(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String
)

/**
 * Modelo de datos para representar un Género.
 */
data class Genre(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String
)

/**
 * Modelo de datos para representar un Editorial.
 */
data class Publisher(
    @SerializedName("id") val id: Int? = null,
    @SerializedName("name") val name: String
)

/**
 * Modelo de solicitud genérica para crear/actualizar catálogos.
 * Solo contiene el campo 'name'.
 */
data class CatalogRequest(
    @SerializedName("name") val name: String
)

/**
 * Modelo de respuesta genérica para mensajes de la API.
 */
data class ApiResponse(
    @SerializedName("message") val message: String
)
