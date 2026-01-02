package com.frabon.captainslog.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.frabon.captainslog.data.Event
import com.frabon.captainslog.data.EventRepository
import com.frabon.captainslog.data.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import java.time.Month
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

class MainViewModel(private val repository: EventRepository) : ViewModel() {

    // Filter States
    private val searchQuery = MutableStateFlow("")
    private val _typeFilter = MutableStateFlow<EventType?>(null) // null means "All"

    // Date Navigation States
    private val _currentMonth = MutableStateFlow(Calendar.getInstance().get(Calendar.MONTH) + 1) // 1-12
    private val _currentYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))

    // UI Label: e.g., "September 2024"
    val currentMonthName = combine(_currentMonth, _currentYear) { month, year ->
        "${Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())} $year"
    }.asLiveData()

    // Main Logic: Filter the raw list from Repository based on current selection
    private val eventsFlow = combine(
        repository.allEvents,
        searchQuery,
        _currentMonth,
        _currentYear,
        _typeFilter
    ) { events, query, month, year, type ->

        events.filter { event ->
            val matchesDate = event.year == year && event.month == month
            val matchesType = type == null || event.type == type
            val matchesQuery = query.isEmpty() || (event.notes?.contains(query, ignoreCase = true) == true)
            matchesDate && matchesType && matchesQuery
        }.sortedWith(
            // Strict Chronological Sorting (Newest First)
            compareByDescending<Event> { it.day }
                .thenByDescending { it.hour }
                .thenByDescending { it.minute }
                // If two events happen at the exact same minute, sort by ID
                .thenByDescending { it.id }
        )
    }

    // Exposed as LiveData for the Fragment to observe
    val groupedEvents = eventsFlow.asLiveData()

    val monthStats = combine(
        repository.allEvents,
        _currentMonth,
        _currentYear
    ) { events, month, year ->
        // 1. Filter only by Date (Ignore Type/Search)
        val currentMonthEvents = events.filter { it.year == year && it.month == month }

        // 2. Count types
        val defecationCount = currentMonthEvents.count { it.type == EventType.DEFECATION }
        val urinationCount = currentMonthEvents.count { it.type == EventType.URINATION }

        // Return a Pair
        Pair(defecationCount, urinationCount)
    }.asLiveData()

    // --- User Actions ---

    fun setSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun setTypeFilter(type: EventType?) {
        _typeFilter.value = type
    }

    fun nextMonth() {
        val currentM = _currentMonth.value
        val currentY = _currentYear.value

        if (currentM == 12) {
            _currentMonth.value = 1
            _currentYear.value = currentY + 1
        } else {
            _currentMonth.value = currentM + 1
        }
    }

    fun previousMonth() {
        val currentM = _currentMonth.value
        val currentY = _currentYear.value

        if (currentM == 1) {
            _currentMonth.value = 12
            _currentYear.value = currentY - 1
        } else {
            _currentMonth.value = currentM - 1
        }
    }

    // --- CSV Export/Import ---

    suspend fun exportEventsToCsv(outputStream: OutputStream) {
        val events = repository.allEvents.first() // Get all data, not just filtered
        val writer = outputStream.bufferedWriter()

        // Header updated with new fields
        writer.write("notes,day,month,year,hour,minute,type\n")

        events.forEach { event ->
            // Handle nullable notes and ensure null safety
            val safeNotes = event.notes?.replace(",", " ") ?: ""
            writer.write("$safeNotes,${event.day},${event.month},${event.year},${event.hour},${event.minute},${event.type}\n")
        }
        writer.flush()
    }

    fun importEventsFromCsv(inputStream: InputStream) = viewModelScope.launch {
        val newEvents = mutableListOf<Event>()
        inputStream.bufferedReader().useLines { lines ->
            lines.drop(1) // Skip header
                .forEach { line ->
                    try {
                        val tokens = line.split(",")
                        // Ensure token count matches expected columns
                        if (tokens.size >= 7) {
                            val event = Event(
                                notes = tokens[0].ifBlank { null },
                                day = tokens[1].toInt(),
                                month = tokens[2].toInt(),
                                year = tokens[3].toInt(),
                                hour = tokens[4].toInt(),
                                minute = tokens[5].toInt(),
                                type = EventType.valueOf(tokens[6])
                            )
                            newEvents.add(event)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
        }
        if (newEvents.isNotEmpty()) {
            repository.insertAllEvents(newEvents)
        }
    }
}