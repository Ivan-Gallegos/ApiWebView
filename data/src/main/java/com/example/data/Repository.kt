package com.example.data

import android.app.Application
import android.util.Log
import com.example.database.SearchDatabase
import com.example.pokeapi.PokeService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class Repository private constructor(
    private val pokeService: PokeService,
    private val db: SearchDatabase,
) {

    companion object {
        val TAG = this::class.simpleName
        val MAX_AGE_MILLIS: Long = TimeUnit.MINUTES.toMillis(10)

        @Volatile
        private var instance: Repository? = null

        fun getInstance(pokeService: PokeService, db: SearchDatabase) =
            instance ?: synchronized(this) {
                instance ?: Repository(pokeService, db).also { instance = it }
            }

        fun getInstance(application: Application): Repository {
            val pokeService = PokeService.getInstance(application)
            val db = SearchDatabase.getInstance(application)
            return getInstance(pokeService, db)
        }
    }

    suspend fun getQueryResponse(query: String): QueryResponse = withContext(Dispatchers.IO) {
        getValidDbResponse(query) ?: getNetworkResponse(query)
    }

    /**
     * @return A response from database which is less than max age
     * or null if no such response exists
     */
    private suspend fun getValidDbResponse(query: String): QueryResponse? =
        withContext(Dispatchers.IO) {
            db.searchDao().getSearchEntity(query).let { entity ->
                if (entity != null && entity.ageMillis <= MAX_AGE_MILLIS) QueryResponse(entity.response)
                else null
            }
        }

    /**
     * Get response from network and store in database if successful
     * @return Response body from network or error message
     */
    private suspend fun getNetworkResponse(query: String): QueryResponse =
        withContext(Dispatchers.IO) {
            try {
                val response = pokeService.getPokemon(query)
                if (response.isSuccessful) (response.body() ?: "Empty body").also {
                    db.searchDao().insertWithTimeStamp(query, it)
                }.let { QueryResponse(it, true) }
                else QueryResponse(response.errorBody()?.string() ?: "Empty Error Message")
            } catch (e: Exception) {
                Log.e(TAG, e.message, e)
                QueryResponse("An Error Has Occurred")
            }
        }

}
