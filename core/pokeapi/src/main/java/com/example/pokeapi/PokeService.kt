package com.example.pokeapi

import android.app.Application
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level.BODY
import okhttp3.logging.HttpLoggingInterceptor.Logger.Companion.DEFAULT
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

interface PokeService {
    companion object {
        private const val BASE_URL = "https://pokeapi.co/api/v2/"
        private const val CACHE_SIZE = 5 * 1024 * 1024L // 5 Mb

        @Volatile
        private var instance: PokeService? = null

        fun getInstance(application: Application): PokeService = instance ?: synchronized(this) {
            instance ?: Cache(application.cacheDir, CACHE_SIZE).let { cache ->
                val client: OkHttpClient = OkHttpClient().newBuilder()
                    .addInterceptor(HttpLoggingInterceptor(DEFAULT).apply { level = BODY })
                    .cache(cache)
                    .build()

                val retrofit: Retrofit = Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .client(client)
                    .build()

                retrofit.create<PokeService>().also { instance = it }
            }
        }
    }

    @GET("pokemon/{name}")
    suspend fun getPokemon(@Path("name") name: String): Response<String>

}
