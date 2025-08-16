package com.example.clientemovil.models // Asegúrate que el nombre del paquete coincida con tu estructura

import com.google.gson.annotations.SerializedName // Importante si usas Gson y los nombres no coinciden exactamente o para _id
import java.util.Date // Para el campo 'date'

/**
 * Representa un detalle individual dentro de una venta.
 * Coincide con `saleDetailSchema` de tu API.
 */
data class SaleDetailResponse(
    @SerializedName("_id") // Si tu API devuelve el _id para cada detalle y lo necesitas
    val detailId: String? = null, // ID del subdocumento de detalle de Mongoose
    val productId: Int,
    val quantity: Int,
    val unitPrice: Double
)

/**
 * Representa la información del usuario populada dentro de una SaleResponse.
 * Coincide con los campos que especificaste en `.populate('userId', 'name role')`.
 */
data class UserInSaleResponse(
    @SerializedName("_id")
    val id: String?,
    val name: String?,
    val role: String?
)

/**
 * Representa la respuesta de una venta completa.
 * Coincide con `saleSchema` y la respuesta de tu API para /sales.
 */
data class SaleResponse(
    @SerializedName("_id") // Para mapear el "_id" de MongoDB a "id" en Kotlin
    val id: String?,        // El ID de la venta.

    val total: Double,
    val date: Date?,        // Retrofit con Gson puede manejar la conversión de fechas ISO a Date.

    val userId: UserInSaleResponse?, // Mapea el objeto 'userId' populado.
    val details: List<SaleDetailResponse>?
)

/**
 * Modelo para la estructura JSON de la respuesta al crear una venta.
 * { "message": "...", "sale": { ... } }
 */
data class CreateSaleApiResponse(
    val message: String,
    val sale: SaleResponse // La venta creada
)

// --- Modelos para la Solicitud de Creación de Venta (SaleRequest) ---

data class SaleDetailRequestItem(
    val productId: Int,
    val quantity: Int
    // unitPrice no se envía en el request, se obtiene del microservicio de productos
)

data class SaleRequest(
    val userId: String, // ID del usuario que realiza la compra
    val details: List<SaleDetailRequestItem>
)
data class SaleCreatedDetail(
    val productId: Int,
    val quantity: Int
)

/**
 * Representa la venta creada, con un userId como String.
 * Este modelo coincide con la respuesta del microservicio de Node.js.
 */
data class SaleCreatedResponse(
    @SerializedName("_id")
    val id: String?,
    val total: Double,
    val userId: String?, // Aquí es donde se espera un String, no un objeto
    val details: List<SaleCreatedDetail>?
)

/**
 * Modelo para la estructura JSON de la respuesta al crear una venta.
 * { "message": "...", "sale": { ... } }
 */
data class CreateSaleSuccessResponse(
    val message: String,
    val sale: SaleCreatedResponse // La venta creada con el modelo simplificado
)

