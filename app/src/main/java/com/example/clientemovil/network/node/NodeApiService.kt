package com.example.clientemovil.network.node

import com.example.clientemovil.models.*
import retrofit2.Response
import retrofit2.http.*

interface NodeApiService {
    @GET("api/users")
    suspend fun getAllUsers(): Response<List<UserWithRoleObject>>

    // Esta es la nueva función para obtener un solo usuario
    @GET("api/users/{id}")
    suspend fun getUserWithRoleString(@Path("id") id: String): Response<User>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<UserWithRoleObject>

    @POST("api/users")
    suspend fun createUser(@Body user: UserWithRoleObject): Response<UserWithRoleObject>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: UserWithRoleObject): Response<UserWithRoleObject>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Unit>

    @GET("api/roles")
    suspend fun getAllRoles(): Response<List<Role>>

    @GET("api/roles/{id}")
    suspend fun getRole(@Path("id") id: String): Response<Role>

    @POST("api/roles")
    suspend fun createRole(@Body role: Role): Response<Role>

    @PUT("api/roles/{id}")
    suspend fun updateRole(@Path("id") id: String, @Body role: Role): Response<Role>

    @DELETE("api/roles/{id}")
    suspend fun deleteRole(@Path("id") id: String): Response<Unit>

    @GET("api/sales")
    suspend fun getAllSales(): Response<List<SaleResponse>>

    @GET("api/sales/{id}")
    suspend fun getSale(@Path("id") id: String): Response<SaleResponse>

    @POST("api/sales")
    suspend fun createSale(@Body sale: SaleRequest): Response<SaleResponse>

    @PUT("api/sales/{id}")
    suspend fun updateSale(@Path("id") id: String, @Body sale: SaleRequest): Response<SaleResponse>

    @DELETE("api/sales/{id}")
    suspend fun deleteSale(@Path("id") id: String): Response<Unit>

    @PUT("api/users/{id}/password")
    suspend fun updatePassword(
        @Path("id") id: String,
        @Body requestBody: Map<String, String>
    ): Response<Unit>
}