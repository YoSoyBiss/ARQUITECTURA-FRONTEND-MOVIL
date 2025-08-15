package com.example.clientemovil.network.laravel

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object LaravelRetrofitClient {
    // Asegúrate de que esta URL sea la correcta para tu microservicio de Laravel
    private const val BASE_URL = "http://10.0.2.2:8000/"

    val api: LaravelApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LaravelApiService::class.java)
    }
}