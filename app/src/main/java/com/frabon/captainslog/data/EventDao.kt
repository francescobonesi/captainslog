package com.frabon.captainslog.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: Event)

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<Event>)

    @Query("SELECT * from events WHERE id = :id")
    fun getEventById(id: Int): Flow<Event>

    // New Query: Filter by Year and Month, ordered by Date and Time descending (newest first)
    @Query("SELECT * FROM events WHERE year = :year AND month = :month ORDER BY day DESC, hour DESC, minute DESC")
    fun getEventsByMonth(year: Int, month: Int): Flow<List<Event>>

    // Optional: If you still need a dump of everything for export
    @Query("SELECT * FROM events ORDER BY year DESC, month DESC, day DESC, hour DESC, minute DESC")
    fun getAllEvents(): Flow<List<Event>>
}