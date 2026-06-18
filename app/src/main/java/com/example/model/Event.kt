package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // Work, Personal, Health, Social, Ideas
    val date: String, // YYYY-MM-DD
    val time: String, // Formatted (e.g. "10:30 AM")
    val hour: Int, // Used for logical chronological sorting
    val minute: Int, // Used for logical chronological sorting
    val isCompleted: Boolean = false,
    val subtasksRaw: String = "", // Serialized as "title:isCompleted|title:isCompleted"
    val isRepeating: Boolean = false,
    val repeatInterval: String = "None" // "None", "Daily", "Weekly"
) {
    // Helper to get parsed subtasks list
    fun getSubtasks(): List<Subtask> {
        return SubtaskHelper.parseSubtasks(subtasksRaw)
    }

    // Builder to create helper subtasks easily
    fun withSubtasks(subtasks: List<Subtask>): Event {
        return this.copy(subtasksRaw = SubtaskHelper.serializeSubtasks(subtasks))
    }
}

data class Subtask(val title: String, val isCompleted: Boolean)

object SubtaskHelper {
    fun parseSubtasks(raw: String): List<Subtask> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|").mapNotNull { part ->
            val colonIndex = part.lastIndexOf(':')
            if (colonIndex != -1) {
                val title = part.substring(0, colonIndex)
                val completedString = part.substring(colonIndex + 1)
                val isCompleted = completedString.lowercase() == "true"
                Subtask(title, isCompleted)
            } else {
                null
            }
        }
    }

    fun serializeSubtasks(subtasks: List<Subtask>): String {
        return subtasks.joinToString("|") { "${it.title.replace(":", "").replace("|", "")}:${it.isCompleted}" }
    }
}
