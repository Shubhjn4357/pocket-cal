package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.EventRepository
import android.content.Context
import com.example.model.Event
import com.example.model.Subtask
import com.example.util.HapticHelper
import com.example.util.NLPParser
import com.example.util.NotificationScheduler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.ui.PocketCalWidgetProvider
import com.example.util.ShortcutHelper

class CalendarViewModel(
    val context: Context,
    private val repository: EventRepository,
    private val hapticHelper: HapticHelper
) : ViewModel() {

    // Helper date formats
    private val dbDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    // Current system date formatted
    val todayDateString: String = dbDateFormat.format(Date())

    // Selected focus date state
    val selectedDate = MutableStateFlow(todayDateString)

    // Category filter state
    val selectedCategoryFilter = MutableStateFlow("All")

    // Theme mode: Adaptive based on OS system setting by default, while supporting dynamic toggle overrides
    val isDarkTheme = MutableStateFlow(
        run {
            val uiMode = context.resources.configuration.uiMode
            (uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES
        }
    )

    // Expose generated sliding ribbon 14-day list centered dynamically around selectedDate
    val weeklyRibbonDays: StateFlow<List<RibbonDay>> = selectedDate
        .map { date -> generateWeeklyRibbon(date) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // User profile settings
    val userName = MutableStateFlow(
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .getString("username", "Shubham Jain") ?: "Shubham Jain"
    )

    val isGoogleConnected = MutableStateFlow(
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .getBoolean("is_google_connected", false)
    )

    val syncGoogleCalendar = MutableStateFlow(
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .getBoolean("sync_google_calendar", true)
    )

    val syncGoogleFit = MutableStateFlow(
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .getBoolean("sync_google_fit", true)
    )

    val notificationLeadTime = MutableStateFlow(
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .getInt("notification_lead_time", 15)
    )

    val isSyncing = MutableStateFlow(false)

    fun setUserName(name: String) {
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .edit().putString("username", name).apply()
        userName.value = name
    }

    fun setGoogleConnected(connected: Boolean) {
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("is_google_connected", connected).apply()
        isGoogleConnected.value = connected
        if (connected) {
            triggerGoogleSyncCombined()
        }
    }

    fun setSyncGoogleCalendar(sync: Boolean) {
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sync_google_calendar", sync).apply()
        syncGoogleCalendar.value = sync
    }

    fun setSyncGoogleFit(sync: Boolean) {
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .edit().putBoolean("sync_google_fit", sync).apply()
        syncGoogleFit.value = sync
    }

    fun setNotificationLeadTime(leadTime: Int) {
        context.getSharedPreferences("pocket_cal_prefs", Context.MODE_PRIVATE)
            .edit().putInt("notification_lead_time", leadTime).apply()
        notificationLeadTime.value = leadTime
        
        // Re-schedule all upcoming alarms so that they match the new lead time offset
        viewModelScope.launch {
            try {
                val dbEvents = repository.getAllEvents().first()
                dbEvents.forEach { event ->
                    if (!event.isCompleted) {
                        NotificationScheduler.scheduleAlarm(context.applicationContext, event)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun triggerGoogleSyncCombined() {
        if (isSyncing.value) return
        viewModelScope.launch {
            isSyncing.value = true
            hapticHelper.playClick()
            
            // Artificial delay to simulate real network sync with Google services
            kotlinx.coroutines.delay(2000)
            
            val activeDay = selectedDate.value
            
            // Sync calendar entries if enabled
            if (syncGoogleCalendar.value) {
                val calEvent = Event(
                    title = "Google Sync: Standup & Board Review",
                    description = "Synchronized from your Google Calendar. Discussing quarterly roadmap metrics and user acquisition pipelines.",
                    category = "Work",
                    date = activeDay,
                    time = "11:30 AM",
                    hour = 11,
                    minute = 30,
                    isCompleted = false
                ).withSubtasks(listOf(
                    Subtask("Pull updated slide deck and metrics", false),
                    Subtask("Share meeting notes summary link", false)
                ))
                repository.insertEvent(calEvent)
                NotificationScheduler.scheduleAlarm(context.applicationContext, calEvent)
            }
            
            // Sync fit entries if enabled
            if (syncGoogleFit.value) {
                val fitEvent = Event(
                    title = "Google Fit: Daily Workout Track",
                    description = "Synchronized from Google Fit. Your target fit goal of 45 minutes cardio active movement achieved!",
                    category = "Health",
                    date = activeDay,
                    time = "06:00 PM",
                    hour = 18,
                    minute = 0,
                    isCompleted = false
                ).withSubtasks(listOf(
                    Subtask("Complete 250 kcal burning target", true),
                    Subtask("Record running pace metric", false)
                ))
                repository.insertEvent(fitEvent)
                NotificationScheduler.scheduleAlarm(context.applicationContext, fitEvent)
            }
            
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            isSyncing.value = false
            hapticHelper.playClick()
        }
    }

    fun selectNextDay() {
        try {
            val date = dbDateFormat.parse(selectedDate.value) ?: return
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            selectedDate.value = dbDateFormat.format(calendar.time)
            hapticHelper.playClick()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun selectPreviousDay() {
        try {
            val date = dbDateFormat.parse(selectedDate.value) ?: return
            val calendar = Calendar.getInstance().apply { time = date }
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            selectedDate.value = dbDateFormat.format(calendar.time)
            hapticHelper.playClick()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Fully reactive flow of events filtered by selected date and active category filter
    @OptIn(ExperimentalCoroutinesApi::class)
    val eventsForSelectedDate: StateFlow<List<Event>> = selectedDate
        .flatMapLatest { date -> repository.getEventsByDate(date) }
        .combine(selectedCategoryFilter) { events, filter ->
            if (filter == "All") {
                events
            } else {
                events.filter { it.category.equals(filter, ignoreCase = true) }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose all dates that contain at least one task, used for the glowing dots
    val datesWithEvents: StateFlow<Set<String>> = repository.getAllEvents()
        .map { events -> events.map { it.date }.toSet() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptySet()
        )

    init {
        // Automatically seed demo schedules if database is completely empty on startup
        seedDemoDataIfNeeded()

        // Reactive live update system: Keeps shortcuts & home widgets perfectly synchronized live
        viewModelScope.launch {
            repository.getEventsByDate(todayDateString).collect { events ->
                ShortcutHelper.updateLauncherShortcuts(context, events)
                try {
                    PocketCalWidgetProvider.triggerWidgetRefresh(context)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // Toggle physical dark mode style
    fun toggleTheme() {
        isDarkTheme.value = !isDarkTheme.value
        hapticHelper.playClick()
    }

    // Trigger explicit manual haptic click
    fun triggerHapticClick() {
        hapticHelper.playClick()
    }

    // Update selected date
    fun selectDate(dateStr: String) {
        selectedDate.value = dateStr
        hapticHelper.playClick()
    }

    // Update filter status
    fun selectCategoryFilter(category: String) {
        selectedCategoryFilter.value = category
        hapticHelper.playClick()
    }

    // Insert new agenda slot using natural language parsing, with repeating loop options and design subtasks list
    fun addEvent(
        rawInput: String,
        manualCategoryOverride: String?,
        optionalDescription: String,
        isRepeating: Boolean = false,
        repeatInterval: String = "None",
        subtasks: List<Subtask> = emptyList()
    ) {
        if (rawInput.isBlank()) return

        val parsed = NLPParser.parse(rawInput)
        val finalCategory = manualCategoryOverride ?: parsed.category
        val finalDescription = if (optionalDescription.isNotBlank()) {
            optionalDescription
        } else {
            "Notes: Created from natural text \"$rawInput\""
        }

        val event = Event(
            title = parsed.title,
            description = finalDescription,
            category = finalCategory,
            date = selectedDate.value,
            time = parsed.time,
            hour = parsed.hour,
            minute = parsed.minute,
            isCompleted = false,
            subtasksRaw = "",
            isRepeating = isRepeating,
            repeatInterval = repeatInterval
        ).withSubtasks(subtasks)

        viewModelScope.launch {
            val insertedId = repository.insertEvent(event)
            val eventWithId = event.copy(id = insertedId.toInt())
            NotificationScheduler.scheduleAlarm(context.applicationContext, eventWithId)
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Update an existing event details using natural language parsing
    fun updateEventDetails(
        id: Int,
        rawInput: String,
        manualCategoryOverride: String?,
        optionalDescription: String,
        isRepeating: Boolean = false,
        repeatInterval: String = "None",
        subtasks: List<Subtask> = emptyList(),
        isCompleted: Boolean = false,
        date: String
    ) {
        if (rawInput.isBlank()) return

        val parsed = NLPParser.parse(rawInput)
        val finalCategory = manualCategoryOverride ?: parsed.category
        val finalDescription = optionalDescription

        val event = Event(
            id = id,
            title = parsed.title,
            description = finalDescription,
            category = finalCategory,
            date = date,
            time = parsed.time,
            hour = parsed.hour,
            minute = parsed.minute,
            isCompleted = isCompleted,
            subtasksRaw = "",
            isRepeating = isRepeating,
            repeatInterval = repeatInterval
        ).withSubtasks(subtasks)

        viewModelScope.launch {
            repository.updateEvent(event)
            if (isCompleted) {
                NotificationScheduler.cancelAlarm(context.applicationContext, event)
            } else {
                NotificationScheduler.scheduleAlarm(context.applicationContext, event)
            }
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Toggle event completion with notifications scheduling
    fun toggleEventCompletion(event: Event) {
        val newStatus = !event.isCompleted
        val subtasks = event.getSubtasks()
        val updatedSubtasks = subtasks.map { it.copy(isCompleted = newStatus) }
        val newEvent = event.copy(isCompleted = newStatus).withSubtasks(updatedSubtasks)

        viewModelScope.launch {
            repository.updateEvent(newEvent)
            if (newStatus) {
                NotificationScheduler.cancelAlarm(context.applicationContext, newEvent)
            } else {
                NotificationScheduler.scheduleAlarm(context.applicationContext, newEvent)
            }
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Toggle subtask within event
    fun toggleSubtask(event: Event, subtaskTitle: String) {
        val currentSubtasks = event.getSubtasks()
        val updatedSubtasks = currentSubtasks.map {
            if (it.title == subtaskTitle) it.copy(isCompleted = !it.isCompleted) else it
        }
        val allCompleted = updatedSubtasks.isNotEmpty() && updatedSubtasks.all { it.isCompleted }
        val newEvent = event.copy(isCompleted = allCompleted)
            .withSubtasks(updatedSubtasks)

        viewModelScope.launch {
            repository.updateEvent(newEvent)
            if (allCompleted) {
                NotificationScheduler.cancelAlarm(context.applicationContext, newEvent)
            } else {
                NotificationScheduler.scheduleAlarm(context.applicationContext, newEvent)
            }
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Add new subtask row inline inside card
    fun addSubtask(event: Event, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        val currentSubtasks = event.getSubtasks().toMutableList()
        currentSubtasks.add(Subtask(subtaskTitle, false))
        
        // When a new incomplete subtask is added, the event should no longer be marked fully complete
        val newEvent = event.copy(isCompleted = false).withSubtasks(currentSubtasks)

        viewModelScope.launch {
            repository.updateEvent(newEvent)
            NotificationScheduler.scheduleAlarm(context.applicationContext, newEvent)
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Delete agenda event and silence any alarms
    fun deleteEvent(event: Event) {
        viewModelScope.launch {
            repository.deleteEvent(event)
            NotificationScheduler.cancelAlarm(context.applicationContext, event)
            PocketCalWidgetProvider.triggerWidgetRefresh(context.applicationContext)
            hapticHelper.playClick()
        }
    }

    // Generate weekly ribbon days: 14 days centered around the selected center date (e.g. centerDate-3 to centerDate+10)
    private fun generateWeeklyRibbon(centerDateString: String? = null): List<RibbonDay> {
        val list = mutableListOf<RibbonDay>()
        val dowFormat = SimpleDateFormat("EEE", Locale.US)
        val domFormat = SimpleDateFormat("d", Locale.US)

        val calendar = Calendar.getInstance()
        if (!centerDateString.isNullOrEmpty()) {
            try {
                dbDateFormat.parse(centerDateString)?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
                // fallback to today if parsing fails
            }
        }
        // Subtract 3 days to start slightly in the past
        calendar.add(Calendar.DAY_OF_YEAR, -3)

        for (i in 0 until 14) {
            val dateStr = dbDateFormat.format(calendar.time)
            val dow = dowFormat.format(calendar.time).uppercase(Locale.US)
            val dom = domFormat.format(calendar.time)
            val isToday = dateStr == todayDateString

            list.add(RibbonDay(dateStr, dow, dom, isToday))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        return list
    }

    // Seeding demo data
    private fun seedDemoDataIfNeeded() {
        viewModelScope.launch {
            try {
                val dbEvents = repository.getAllEvents().first()
                if (dbEvents.isEmpty()) {
                    val demo1 = Event(
                        title = "Gym Power Session",
                        description = "Ready for fitness routines & workout performance",
                        category = "Health",
                        date = todayDateString,
                        time = "07:30 AM",
                        hour = 7,
                        minute = 30,
                        isCompleted = false
                    ).withSubtasks(listOf(
                        Subtask("Hydrate (1.5L water)", false),
                        Subtask("Proper dynamic warmups", false)
                    ))

                    val demo2 = Event(
                        title = "Chronos Task Sync",
                        description = "Engineering sprint sync with the design & product teams",
                        category = "Work",
                        date = todayDateString,
                        time = "10:30 AM",
                        hour = 10,
                        minute = 30,
                        isCompleted = false
                    ).withSubtasks(listOf(
                        Subtask("Prepare design deck", false),
                        Subtask("Performance profile metrics", false)
                    ))

                    val demo3 = Event(
                        title = "Espresso chat with friends",
                        description = "Local café breakout brainstorm to sync up prototypes",
                        category = "Social",
                        date = todayDateString,
                        time = "04:30 PM",
                        hour = 16,
                        minute = 30,
                        isCompleted = false
                    ).withSubtasks(listOf(
                        Subtask("Showcase active prototype", false)
                    ))

                    repository.insertEvent(demo1)
                    repository.insertEvent(demo2)
                    repository.insertEvent(demo3)
                }
            } catch (e: Exception) {
                // Log/handle potential exceptions gracefully
            }
        }
    }

    // Helper functions for formatting UI date headings
    fun getFormattedHeadingDate(dateStr: String): String {
        return try {
            val date = dbDateFormat.parse(dateStr) ?: return dateStr
            val sdf = SimpleDateFormat("MMMM yyyy", Locale.US)
            sdf.format(date)
        } catch (e: Exception) {
            dateStr
        }
    }
}

data class RibbonDay(
    val dateString: String, // "YYYY-MM-DD"
    val dayOfWeek: String,  // "MON"
    val dayOfMonth: String, // "17"
    val isToday: Boolean
)

class CalendarViewModelFactory(
    private val context: Context,
    private val repository: EventRepository,
    private val hapticHelper: HapticHelper
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(context, repository, hapticHelper) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
