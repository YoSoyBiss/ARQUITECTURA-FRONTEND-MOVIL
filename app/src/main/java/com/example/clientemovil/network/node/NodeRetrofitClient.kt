package com.example.clientemovil.network.node

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NodeRetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:5000/"

    val api: NodeApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NodeApiService::class.java)
    }
}