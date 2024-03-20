package com.example.data

import com.example.pokeapi.PokeService


class Repository private constructor(private val pokeService: PokeService) {

    companion object {
        @Volatile
        private var instance: Repository? = null

        fun getInstance(pokeService: PokeService) = instance ?: synchronized(this) {
            instance ?: Repository(pokeService).also { instance = it }
        }
    }

    suspend fun getQueryResponse(query: String) = pokeService.getPokemon(query)

}
