package com.udaan.child.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class OptimizationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return Result.success()
    }
}
