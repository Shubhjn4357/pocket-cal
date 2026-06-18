package com.example.ui

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.model.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PocketCalWidgetProvider : AppWidgetProvider() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val pendingResult = goAsync()
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
        val displayDateStr = SimpleDateFormat("MMM d, EEE", Locale.US).format(Date())

        scope.launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val events = try {
                    db.eventDao().getEventsByDateSync(todayStr)
                } catch (e: Exception) {
                    emptyList()
                }

                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, displayDateStr, events)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    pendingResult.finish()
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PocketCalWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(component)
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        displayDateStr: String,
        events: List<Event>
    ) {
        val views = RemoteViews(context.packageName, R.layout.pocket_cal_widget)

        views.setTextViewText(R.id.widget_date, displayDateStr)

        if (events.isEmpty()) {
            views.setTextViewText(R.id.widget_task_status, "No remaining tasks today")
            views.setViewVisibility(R.id.widget_next_task_container, View.GONE)
        } else {
            val incompleteTasks = events.filter { !it.isCompleted }
            if (incompleteTasks.isEmpty()) {
                views.setTextViewText(R.id.widget_task_status, "All ${events.size} tasks completed! ✨")
                views.setViewVisibility(R.id.widget_next_task_container, View.GONE)
            } else {
                views.setTextViewText(
                    R.id.widget_task_status,
                    "${incompleteTasks.size} tasks remaining today"
                )
                views.setViewVisibility(R.id.widget_next_task_container, View.VISIBLE)
                
                val nextTask = incompleteTasks.first()
                views.setTextViewText(R.id.widget_next_task_title, nextTask.title)
                views.setTextViewText(R.id.widget_next_task_time, nextTask.time)
            }
        }

        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_background, pendingIntent)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.example.ui.ACTION_REFRESH_WIDGET"

        fun triggerWidgetRefresh(context: Context) {
            val intent = Intent(context, PocketCalWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }
}
