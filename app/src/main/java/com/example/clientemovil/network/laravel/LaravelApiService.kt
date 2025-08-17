package com.example.clientemovil.network.laravel

import com.example.clientemovil.models.*

import retrofit2.Response
import retrofit2.http.*

interface LaravelApiService {

    // --- Rutas para Productos ---
    @GET("api/products")
    suspend fun getAllProducts(): Response<List<Product>>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: Int): Response<Product>

    // MODIFICADO: Usa el nuevo modelo ProductRequest para la CREACIÓN
    @POST("api/products")
    suspend fun createProduct(@Body productRequest: ProductRequest): Response<Product>

    @PUT("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: Product): Response<Product>

    @DELETE("api/products/{id}")
    suspend fun deleteProduct(@Path("id") id: Int): Response<Void>

    @PATCH("api/products/{id}")
    suspend fun updateProduct(@Path("id") id: Int, @Body product: ProductUpdateRequest): Response<Product>


    // --- Rutas para Autores ---
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


    // --- Rutas para Géneros ---
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


    // --- Rutas para Editoriales ---
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