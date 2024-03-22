package com.example.data

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.database.SearchDatabase

class DeleteSearchEntityWorker(
    appContext: Context, workerParameters: WorkerParameters
) : Worker(appContext, workerParameters) {
    companion object {
        const val QUERY_KEY = "query"
        private val TAG = DeleteSearchEntityWorker::class.simpleName
    }
    private val query = workerParameters.inputData.getString(QUERY_KEY)

    override fun doWork(): Result {
        return query?.let {
            SearchDatabase.getInstance(applicationContext).searchDao().deleteByQuery(it)
            Log.d(TAG,"Row deleted for: $query")
            Result.success()
        } ?: Result.failure()
    }
}
