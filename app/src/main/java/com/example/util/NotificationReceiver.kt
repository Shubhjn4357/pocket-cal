package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getIntExtra("event_id", 0)
        val eventTitle = intent.getStringExtra("event_title") ?: "Scheduled Task"
        val eventCategory = intent.getStringExtra("event_category") ?: "Reminder"
        val eventTime = intent.getStringExtra("event_time") ?: ""

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "pocket_cal_reminders"

        // Create Channel on Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Pocket Cal Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifies of scheduled agenda slots in Pocket Cal"
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Open app on notification click
        val clickIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            eventId,
            clickIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val categoryAccent = when (eventCategory) {
            "Work" -> "💼"
            "Personal" -> "❤️"
            "Health" -> "🏃"
            "Social" -> "☕"
            "Ideas" -> "💡"
            else -> "⏰"
        }

        val sharedPrefs = context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
        val leadTimeMinutes = sharedPrefs.getInt("notification_lead_time", 15)
        val contentText = if (leadTimeMinutes == 0) {
            "Starting now ($eventTime). Secure your agenda!"
        } else {
            "Starts in $leadTimeMinutes minutes ($eventTime). Prepare your agenda!"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm) // System standard backup icon, styled neatly by OS
            .setContentTitle("$categoryAccent $eventTitle")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(eventId, notification)
    }
}
