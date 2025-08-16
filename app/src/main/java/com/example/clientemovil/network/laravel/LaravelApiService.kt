package com.example.clientemovil.network.laravel

import com.example.clientemovil.models.Product
import com.example.clientemovil.models.Author
import com.example.clientemovil.models.Genre
import com.example.clientemovil.models.Publisher
import com.example.clientemovil.models.CatalogRequest
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

    // --- Rutas para Autores (CORREGIDAS) ---
    @GET("api/authors")
    suspend fun getAllAuthors(): Response<List<Author>>

    @GET("api/authors/{id}")
    suspend fun getAuthor(@Path("id") id: Int): Response<Author>

    @POST("api/authors")
    suspend fun createAuthor(@Body catalogRequest: CatalogRequest): Response<Author>

    @PUT("api/authors/{id}")
    suspend fun updateAuthor(@Path("id") id: Int, @Body catalogRequest: CatalogRequest): Response<Author>

    @DELETE("api/authors/{id}")
    suspend fun deleteAuthor(@Path("id") id: Int): Response<Unit>


    // --- Rutas para Géneros (CORREGIDAS) ---
    @GET("api/genres")
    suspend fun getAllGenres(): Response<List<Genre>>

    @GET("api/genres/{id}")
    suspend fun getGenre(@Path("id") id: Int): Response<Genre>

    @POST("api/genres")
    suspend fun createGenre(@Body catalogRequest: CatalogRequest): Response<Genre>

    @PUT("api/genres/{id}")
    suspend fun updateGenre(@Path("id") id: Int, @Body catalogRequest: CatalogRequest): Response<Genre>

    @DELETE("api/genres/{id}")
    suspend fun deleteGenre(@Path("id") id: Int): Response<Unit>


    // --- Rutas para Editoriales (CORREGIDAS) ---
    @GET("api/publishers")
    suspend fun getAllPublishers(): Response<List<Publisher>>

    @GET("api/publishers/{id}")
    suspend fun getPublisher(@Path("id") id: Int): Response<Publisher>

    @POST("api/publishers")
    suspend fun createPublisher(@Body catalogRequest: CatalogRequest): Response<Publisher>

    @PUT("api/publishers/{id}")
    suspend fun updatePublisher(@Path("id") id: Int, @Body catalogRequest: CatalogRequest): Response<Publisher>

    @DELETE("api/publishers/{id}")
    suspend fun deletePublisher(@Path("id") id: Int): Response<Unit>
}
