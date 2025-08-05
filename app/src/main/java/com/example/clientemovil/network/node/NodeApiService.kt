package com.example.clientemovil.network.node


import com.example.clientemovil.models.User
import com.example.clientemovil.models.Sale
import retrofit2.Response
import retrofit2.http.*

interface NodeApiService {

    @POST("api/users/login")
    suspend fun login(@Body credentials: Map<String, String>): Response<Map<String, Any>>

    @POST("api/users/register")
    suspend fun register(@Body user: User): Response<Map<String, Any>>

    @GET("api/users")
    suspend fun getAllUsers(): Response<List<User>>

    @GET("api/users/{id}")
    suspend fun getUser(@Path("id") id: String): Response<User>

    @PUT("api/users/{id}")
    suspend fun updateUser(@Path("id") id: String, @Body user: User): Response<Map<String, Any>>

    @DELETE("api/users/{id}")
    suspend fun deleteUser(@Path("id") id: String): Response<Map<String, Any>>

    @GET("api/sales")
    suspend fun getAllSales(): Response<List<Sale>>

    @POST("api/sales")
    suspend fun createSale(@Body sale: Sale): Response<Map<String, Any>>
}
