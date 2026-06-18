package com.example.util

import com.example.model.Event
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class ParsedSchedule(
    val title: String,
    val category: String,
    val time: String,
    val hour: Int,
    val minute: Int
)

object NLPParser {
    // Categories groups keywords
    private val HEALTH_KEYWORDS = listOf("gym", "workout", "run", "doctor", "yoga", "exercise")
    private val WORK_KEYWORDS = listOf("team", "meeting", "office", "code", "client", "sync", "presentation")
    private val SOCIAL_KEYWORDS = listOf("lunch", "dinner", "drinks", "coffee", "party", "friend")
    private val IDEAS_KEYWORDS = listOf("draft", "design", "write", "creative", "thought", "sketch")

    // Match patterns containing prefix triggers (e.g. at 11 AM, @ 10:30 PM, at 7:00).
    // Note: Use a non-word boundary or space check around the triggers to prevent failures.
    private val PREFIX_PATTERN = "(?i)(?:\\bat\\b|@)\\s*(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)?\\b".toRegex()

    // Fallback loose pattern
    private val LOOSE_PATTERN = "(?i)\\b(\\d{1,2})(?::(\\d{2}))?\\s*(AM|PM)\\b".toRegex()

    fun parse(input: String): ParsedSchedule {
        // Find category
        val category = extractCategory(input)

        // Find time
        var hour = 12
        var minute = 0
        var amPm = "PM"
        var matchedText = ""

        val prefixMatch = PREFIX_PATTERN.find(input)
        if (prefixMatch != null) {
            matchedText = prefixMatch.value
            val hVal = prefixMatch.groupValues[1].toInt()
            val mVal = if (prefixMatch.groupValues[2].isNotEmpty()) prefixMatch.groupValues[2].toInt() else 0
            val meridian = prefixMatch.groupValues[3]

            val parsedTime = resolveTime(hVal, mVal, meridian.ifEmpty { null })
            hour = parsedTime.first
            minute = parsedTime.second
            amPm = parsedTime.third
        } else {
            val looseMatch = LOOSE_PATTERN.find(input)
            if (looseMatch != null) {
                matchedText = looseMatch.value
                val hVal = looseMatch.groupValues[1].toInt()
                val mVal = if (looseMatch.groupValues[2].isNotEmpty()) looseMatch.groupValues[2].toInt() else 0
                val meridian = looseMatch.groupValues[3]

                val parsedTime = resolveTime(hVal, mVal, meridian)
                hour = parsedTime.first
                minute = parsedTime.second
                amPm = parsedTime.third
            }
        }

        // Clean up title
        var title = input
        if (matchedText.isNotEmpty()) {
            title = title.replace(matchedText, "")
        }
        // General cleanup of keywords like left-alone "@" or "at"
        title = title.replace(Regex("(?i)\\b(?:at|@)\\s*$"), "")
        title = title.replace(Regex("(?i)^\\s*(?:at|@)\\s*"), "")
        title = title.replace(Regex("\\s+"), " ").trim()
        
        if (title.endsWith("at", ignoreCase = true)) {
            title = title.substring(0, title.length - 2).trim()
        }
        if (title.endsWith("@")) {
            title = title.substring(0, title.length - 1).trim()
        }

        // If title is blank, fallback to a sensible name based on category
        if (title.isBlank()) {
            title = when (category) {
                "Health" -> "Gym & Wellness Session"
                "Work" -> "Work Sync Task"
                "Social" -> "Meetup & Chats"
                "Ideas" -> "Creative Sketch Slot"
                else -> "New Agenda Slot"
            }
        }

        val formattedTime = String.format(Locale.US, "%02d:%02d %s", if (hour == 0 || hour == 12) 12 else hour % 12, minute, amPm)

        return ParsedSchedule(
            title = title,
            category = category,
            time = formattedTime,
            hour = hour,
            minute = minute
        )
    }

    private fun extractCategory(text: String): String {
        val lower = text.lowercase(Locale.US)
        
        if (HEALTH_KEYWORDS.any { lower.contains(it) }) return "Health"
        if (WORK_KEYWORDS.any { lower.contains(it) }) return "Work"
        if (SOCIAL_KEYWORDS.any { lower.contains(it) }) return "Social"
        if (IDEAS_KEYWORDS.any { lower.contains(it) }) return "Ideas"
        
        return "Personal"
    }

    private fun resolveTime(h: Int, m: Int, meridian: String?): Triple<Int, Int, String> {
        var hour = h.coerceIn(0, 23)
        val minute = m.coerceIn(0, 59)
        var amPmStr = "PM"

        if (meridian != null) {
            val upperMeridian = meridian.uppercase(Locale.US)
            amPmStr = upperMeridian
            if (upperMeridian == "PM") {
                if (hour < 12) {
                    hour += 12
                }
            } else if (upperMeridian == "AM") {
                if (hour == 12) {
                    hour = 0
                }
            }
        } else {
            // "If no meridian details (AM/PM) are explicitly written, default to PM for lower morning figures (e.g., 6 implies 18:00 / 6 PM)."
            // Let's implement this: if hour is 1 to 8, we assume PM. If 9 to 11, we assume AM. If 12, PM. If 0, AM.
            if (hour in 1..8) {
                hour += 12
                amPmStr = "PM"
            } else if (hour in 9..11) {
                amPmStr = "AM"
            } else if (hour == 12) {
                amPmStr = "PM"
            } else if (hour == 0) {
                amPmStr = "AM"
            } else {
                // If it's already 13..23, it is PM
                amPmStr = "PM"
            }
        }

        return Triple(hour, minute, amPmStr)
    }
}
