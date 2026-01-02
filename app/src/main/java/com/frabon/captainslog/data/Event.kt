package com.frabon.captainslog.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val notes: String? = null,
    val day: Int,
    val month: Int,
    val year: Int,
    val hour: Int,
    val minute: Int,
    val type: EventType
)