package com.example.medisight.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.medisight.helper.NotificationHelper
import com.example.medisight.helper.PreferencesHelper

class WoundCareWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            Log.d(TAG, "Starting WoundCareWorker")

            val preferencesHelper = PreferencesHelper(applicationContext)
            if (!preferencesHelper.isNotificationEnabled()) {
                Log.d(TAG, "Notifications are disabled")
                return Result.success()
            }

            val woundType = inputData.getString(KEY_WOUND_TYPE) ?: DEFAULT_WOUND_TYPE
            Log.d(TAG, "Processing wound type: $woundType")

            NotificationHelper(applicationContext).showDailyReminder(woundType)

            Log.d(TAG, "Successfully showed daily reminder")
            return Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in WoundCareWorker", e)
            return Result.failure()
        }
    }

    companion object {
        private const val TAG = "WoundCareWorker"
        const val KEY_WOUND_TYPE = "wound_type"
        private const val DEFAULT_WOUND_TYPE = "general"
    }
}
