package com.example.medisight.helper

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.medisight.R

class NotificationHelper(private val context: Context) {
    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Channel for wound care reminders"
            }

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showImmediateNotification(woundType: String) {
        try {
            val message = getInitialWoundCareMessage(woundType)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Wound Care Instructions")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_IMMEDIATE_ID, builder.build())

            Log.d(TAG, "Successfully showed immediate notification for $woundType")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing immediate notification", e)
        }
    }

    fun showDailyReminder(woundType: String) {
        try {
            val message = getDailyReminderMessage(woundType)
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle("Daily Wound Care Reminder")
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)

            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_DAILY_ID, builder.build())

            Log.d(TAG, "Successfully showed daily reminder for $woundType")
        } catch (e: Exception) {
            Log.e(TAG, "Error showing daily reminder", e)
        }
    }

    private fun getInitialWoundCareMessage(woundType: String): String {
        return when (woundType.lowercase()) {
            "burns" -> "Important: Clean the burn area immediately with cool water. Apply prescribed burn ointment and keep the area protected. Monitor for signs of infection."
            "abrasions" -> "Important: Clean the abrasion thoroughly with antiseptic solution. Keep the wound clean and dry. Apply recommended antibiotic ointment."
            "cut" -> "Important: Apply direct pressure to stop any bleeding. Clean the cut thoroughly with antiseptic. Keep the wound covered and dry."
            "bruises" -> "Important: Apply cold compress for 15 minutes. Rest the affected area and elevate if possible. Monitor for severe pain or swelling."
            else -> "Important: Keep your wound clean and protected. Monitor for any signs of infection like increased pain, redness, or swelling."
        }
    }

    private fun getDailyReminderMessage(woundType: String): String {
        return when (woundType.lowercase()) {
            "burns" -> "Time to check your burn wound. Clean gently and apply medication as prescribed. Watch for any signs of infection."
            "abrasions" -> "Remember to clean your abrasion and change dressing if needed. Keep the area dry and protected."
            "cut" -> "Time to check your cut wound. Clean the area and change dressing if necessary. Monitor healing progress."
            "bruises" -> "Check your bruise healing progress. Continue with cold/warm compress as needed."
            else -> "Time for your daily wound care. Keep the wound clean and watch for any concerning changes."
        }
    }

    companion object {
        private const val TAG = "NotificationHelper"
        const val CHANNEL_ID = "wound_care_channel"
        const val CHANNEL_NAME = "Wound Care Reminders"
        const val NOTIFICATION_IMMEDIATE_ID = 1
        const val NOTIFICATION_DAILY_ID = 2
    }
}