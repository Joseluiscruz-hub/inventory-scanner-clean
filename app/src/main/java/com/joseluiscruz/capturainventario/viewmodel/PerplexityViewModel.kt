package com.joseluiscruz.capturainventario.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joseluiscruz.capturainventario.data.perplexity.PerplexityResponse
import com.joseluiscruz.capturainventario.domain.perplexity.PerplexityRepository
import kotlinx.coroutines.launch

class PerplexityViewModel : ViewModel() {

    private val repository = PerplexityRepository()

    private val _answer = MutableLiveData<PerplexityResponse>()
    val answer: LiveData<PerplexityResponse> = _answer

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun getAnswer(token: String, input: String) {
        viewModelScope.launch {
            try {
                val response = repository.getAnswer("Bearer $token", input)
                _answer.postValue(response)
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}

