package com.joseluiscruz.capturainventario.domain.perplexity

import com.joseluiscruz.capturainventario.data.perplexity.PerplexityApi
import com.joseluiscruz.capturainventario.data.perplexity.PerplexityRequest
import com.joseluiscruz.capturainventario.data.perplexity.PerplexityResponse
import com.joseluiscruz.capturainventario.data.perplexity.RetrofitClient

class PerplexityRepository {
    private val perplexityApi: PerplexityApi = RetrofitClient.instance

    suspend fun getAnswer(token: String, input: String): PerplexityResponse {
        val request = PerplexityRequest(input)
        return perplexityApi.getAnswer(token, request)
    }
}

