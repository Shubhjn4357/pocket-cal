package com.example.util

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import android.os.Build
import com.example.MainActivity
import com.example.R
import com.example.model.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ShortcutHelper {
    /**
     * Dynamically update launcher long-press shortcuts with the live date 
     * and today's calendar/tasks status summary to satisfy the live-app-icon date update.
     */
    fun updateLauncherShortcuts(context: Context, events: List<Event>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        val shortcutManager = context.getSystemService(ShortcutManager::class.java) ?: return

        val displayDate = SimpleDateFormat("EEE, MMM d", Locale.US).format(Date())
        val incomplete = events.filter { !it.isCompleted }

        val statusLabel = when {
            events.isEmpty() -> "No tasks scheduled today"
            incomplete.isEmpty() -> "All ${events.size} tasks done! ✨"
            else -> "${incomplete.size} tasks remaining today"
        }

        // Dynamic Shortcut 1: Live Date & Tasks Status Check
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val statusShortcut = ShortcutInfo.Builder(context, "id_live_status")
            .setShortLabel(displayDate)
            .setLongLabel("Today: $statusLabel")
            .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
            .setIntent(mainIntent)
            .build()

        // Dynamic Shortcut 2: Quick Add Agenda Item
        val addIntent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_QUICK_ADD_AGENDAS"
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        
        val addShortcut = ShortcutInfo.Builder(context, "id_add_agenda")
            .setShortLabel("New Agenda")
            .setLongLabel("Secure a new Agenda Slot")
            .setIcon(Icon.createWithResource(context, android.R.drawable.ic_input_add))
            .setIntent(addIntent)
            .build()

        try {
            shortcutManager.dynamicShortcuts = listOf(statusShortcut, addShortcut)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
