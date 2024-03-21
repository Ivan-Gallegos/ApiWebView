package com.example.domain

import com.example.data.Repository
import com.example.model.SearchResponse

class GetSearchResponseUseCase(private val repository: Repository) {
    suspend operator fun invoke(query: String): SearchResponse = repository.getQueryResponse(query)
}