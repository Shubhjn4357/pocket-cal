package com.example.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.example.model.Event
import java.util.Calendar

object NotificationScheduler {

    fun scheduleAlarm(context: Context, event: Event) {
        if (event.isCompleted) {
            cancelAlarm(context, event)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Compute fire time
        val dateParts = event.date.split("-")
        if (dateParts.size != 3) return

        val year = dateParts[0].toIntOrNull() ?: return
        val month = (dateParts[1].toIntOrNull() ?: return) - 1 // 0-based index
        val day = dateParts[2].toIntOrNull() ?: return

        val alarmCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, event.hour)
            set(Calendar.MINUTE, event.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Subtraction of customizable notification lead time (e.g., 15 minutes before)
        val sharedPrefs = context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
        val leadTimeMinutes = sharedPrefs.getInt("notification_lead_time", 15)
        alarmCal.add(Calendar.MINUTE, -leadTimeMinutes)

        val now = Calendar.getInstance()
        
        // If alarm time is in the past, and it's repeating, adjust to next intervals or skip
        if (alarmCal.before(now)) {
            if (event.isRepeating) {
                while (alarmCal.before(now)) {
                    when (event.repeatInterval) {
                        "Weekly" -> alarmCal.add(Calendar.DAY_OF_YEAR, 7)
                        "Yearly" -> alarmCal.add(Calendar.YEAR, 1)
                        else -> alarmCal.add(Calendar.DAY_OF_YEAR, 1)
                    }
                }
            } else {
                // Past single events don't trigger
                return
            }
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("event_id", event.id)
            putExtra("event_title", event.title)
            putExtra("event_category", event.category)
            putExtra("event_time", event.time)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (event.isRepeating) {
                val intervalMillis = when (event.repeatInterval) {
                    "Weekly" -> AlarmManager.INTERVAL_DAY * 7
                    "Yearly" -> AlarmManager.INTERVAL_DAY * 365
                    else -> AlarmManager.INTERVAL_DAY // Daily or other default
                }
                alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    alarmCal.timeInMillis,
                    intervalMillis,
                    pendingIntent
                )
            } else {
                alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    alarmCal.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            // Graceful logging / handling
        }
    }

    fun cancelAlarm(context: Context, event: Event) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )

        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }
}
