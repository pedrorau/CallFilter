package com.pedrorau.callfilter.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

class BlockNotificationManager(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "blocked_calls"
        private const val CHANNEL_NAME = "Blocked Calls"
    }

    init {
        createNotificationChannel()
    }

    fun showBlockedNotification(phoneNumber: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_call)
            .setContentTitle("Call blocked")
            .setContentText("Blocked call from $phoneNumber")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Time-based so IDs stay unique across process restarts and don't replace earlier notifications.
        val notificationId = (System.currentTimeMillis() % Int.MAX_VALUE).toInt()
        manager.notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for blocked calls"
        }
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }
}
