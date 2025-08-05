package com.example.clientemovil.network.laravel

import com.example.clientemovil.models.Product
import retrofit2.Response
import retrofit2.http.*

interface LaravelApiService {

    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>

    @POST("api/products")
    suspend fun createProduct(@Body product: Product): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Void>
}