package com.joseluiscruz.capturainventario.data.perplexity

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "https://api.perplexity.ai/"

    val instance: PerplexityApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(PerplexityApi::class.java)
    }
}

