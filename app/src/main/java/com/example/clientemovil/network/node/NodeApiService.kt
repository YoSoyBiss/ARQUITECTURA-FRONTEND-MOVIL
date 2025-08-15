package com.example.clientemovil.network.node

import com.example.clientemovil.models.SaleRequest
import com.example.clientemovil.models.SaleResponse
import com.example.clientemovil.models.UserWithRoleObject
import com.example.clientemovil.models.Role
import retrofit2.Response
import retrofit2.http.*
import retrofit2.http.Body

interface NodeApiService {

    // Rutas para Usuarios
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserWithRoleObject>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserWithRoleObject>

    @POST("api/users")
    suspend fun createUser(@Body user: UserWithRoleObject): Response<UserWithRoleObject>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserWithRoleObject): Response<UserWithRoleObject>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Void>

    // Rutas para Roles
    @GET("api/roles")
    suspend fun getAllRoles(): Response<List<Role>>

    @GET("api/roles/{id}") // <- El método que faltaba para cargar un solo rol
    suspend fun getRole(@Path("id") id: String): Response<Role>

    @POST("api/roles")
    suspend fun createRole(@Body role: Role): Response<Role>

    @PUT("api/roles/{id}")
    suspend fun updateRole(@Path("id") id: String, @Body role: Role): Response<Role>

    @DELETE("api/roles/{id}")
    suspend fun deleteRole(@Path("id") id: String): Response<Void>

    // Rutas para Ventas
    @GET("api/sales")
    suspend fun getAllSales(): Response<List<SaleResponse>>

    @GET("api/sales/{id}")
    suspend fun getSale(@Path("id") id: String): Response<SaleResponse>

    @POST("api/sales")
    suspend fun createSale(@Body sale: SaleRequest): Response<SaleResponse>

    @PUT("api/sales/{id}")
    suspend fun updateSale(@Path("id") id: String, @Body sale: Map<String, Any>): Response<Void>

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(@Path("id") id: String): Response<Void>
}