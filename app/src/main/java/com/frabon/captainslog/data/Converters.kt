package com.frabon.captainslog.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromEventType(type: EventType): String {
        return type.name
    }

    @TypeConverter
    fun toEventType(value: String): EventType {
        return EventType.valueOf(value)
    }
}