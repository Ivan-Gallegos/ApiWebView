package com.example.data

import android.app.Application
import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.data.DeleteSearchEntityWorker.Companion.QUERY_KEY
import com.example.database.SearchDatabase
import com.example.model.SearchResponse
import com.example.pokeapi.PokeService
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class Repository private constructor(
    private val pokeService: PokeService,
    private val db: SearchDatabase,
    private val wm: WorkManager
) {

    companion object {
        val TAG = this::class.simpleName
        val MAX_AGE_MILLIS: Long = TimeUnit.MINUTES.toMillis(10)
        val DELETE_DELAY_MILLIS: Long = TimeUnit.MINUTES.toMillis(30)

        @Volatile
        private var instance: Repository? = null

        fun getInstance(pokeService: PokeService, db: SearchDatabase, wm: WorkManager) =
            instance ?: synchronized(this) {
                instance ?: Repository(pokeService, db, wm).also { instance = it }
            }

        fun getInstance(application: Application): Repository {
            val pokeService = PokeService.getInstance(application)
            val db = SearchDatabase.getInstance(application)
            val wm = WorkManager.getInstance(application)
            return getInstance(pokeService, db, wm)
        }
    }

    suspend fun getQueryResponse(query: String): SearchResponse = withContext(IO) {
        getValidDbResponse(query) ?: getNetworkResponse(query)
    }

    /**
     * @return A response from database which is less than max age
     * or null if no such response exists
     */
    private suspend fun getValidDbResponse(query: String): SearchResponse? = withContext(IO) {
        db.searchDao().getSearchEntity(query).let { entity ->
            if (entity != null && entity.ageMillis <= MAX_AGE_MILLIS) SearchResponse(entity.response)
            else null
        }
    }

    /**
     * Get response from network and store in database if successful
     * @return Response body from network or error message
     */
    private suspend fun getNetworkResponse(query: String): SearchResponse = withContext(IO) {
        try {
            val response = pokeService.getPokemon(query)
            if (response.isSuccessful) (response.body() ?: "Empty body").also {
                db.searchDao().insertWithTimeStamp(query, it)
                scheduleDeletion(query)
            }.let { SearchResponse(it, fromNetwork = true) }
            else SearchResponse(response.errorBody()?.string() ?: "Empty Error Message")
        } catch (e: Exception) {
            Log.e(TAG, e.message, e)
            SearchResponse("An Error Has Occurred")
        }
    }

    private fun scheduleDeletion(query: String) {
        val deleteSearchEntityRequest =
            OneTimeWorkRequestBuilder<DeleteSearchEntityWorker>().setInitialDelay(
                DELETE_DELAY_MILLIS, TimeUnit.MILLISECONDS
            ).setInputData(workDataOf(QUERY_KEY to query)).build()
        wm.enqueue(deleteSearchEntityRequest)
    }

}
