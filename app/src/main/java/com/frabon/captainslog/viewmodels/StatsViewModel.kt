package com.frabon.captainslog.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.frabon.captainslog.data.Event
import com.frabon.captainslog.data.EventRepository
import com.frabon.captainslog.data.EventType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.ChronoUnit

// Helper data classes
data class StreakInfo(
    val length: Int,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isActive: Boolean
)

data class MonthlyReport(
    val month: Month,
    val totalCount: Int,
    val averagePerDay: Double,
    val modePerDay: Int
)

class StatsViewModel(private val repository: EventRepository) : ViewModel() {

    // Inputs
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)

    // 1. Raw Data (Defecation Only)
    private val allDefecationEvents = repository.allEvents.map { list ->
        list.filter { it.type == EventType.DEFECATION }
            .sortedWith(compareBy({ it.year }, { it.month }, { it.day }))
    }

    // 2. YEARLY STATISTICS & MONTHLY BREAKDOWN
    val yearStats = combine(allDefecationEvents, _selectedYear) { events, year ->
        val yearEvents = events.filter { it.year == year }
        val today = LocalDate.now()

        Month.entries.map { month ->
            val monthEvents = yearEvents.filter { it.month == month.value }
            val total = monthEvents.size

            val yearMonth = YearMonth.of(year, month)
            val daysInMonth = yearMonth.lengthOfMonth()

            // LOGIC CHANGE 1: Average Calculation
            // If it's the current month of the current year, use days passed so far (up to today)
            // Otherwise use the full length of the month
            val daysForAverage = if (year == today.year && month == today.month) {
                today.dayOfMonth
            } else {
                daysInMonth
            }

            // Populate daily counts for Mode calculation
            val dailyCounts = IntArray(daysForAverage) { 0 }

            monthEvents.forEach { event ->
                // Only count events that happened within the valid range (1..daysForAverage)
                if (event.day in 1..daysForAverage) {
                    dailyCounts[event.day - 1]++
                }
            }

            // Calculate Stats
            val average = if (daysForAverage > 0) total.toDouble() / daysForAverage else 0.0

            // LOGIC CHANGE 2: Mode Calculation
            val mode = calculateMode(dailyCounts)

            MonthlyReport(month, total, average, mode)
        }
    }.asLiveData()

    // 3. OVERALL YEAR SUMMARY
    val yearSummary = combine(allDefecationEvents, _selectedYear) { events, year ->
        val yearEvents = events.filter { it.year == year }
        val total = yearEvents.size

        val today = LocalDate.now()

        // Calculate the denominator (daysToCount)
        val daysToCount: Long = when {
            // CASE A: Future Year (Shouldn't happen via UI, but safe to handle)
            year > today.year -> 0

            // CASE B: Past Year -> Use full days in year
            year < today.year -> {
                if (YearMonth.of(year, 2).isLeapYear) 366L else 365L
            }

            // CASE C: Current Year -> The Dynamic Logic
            else -> {
                // 1. Are there events in previous years?
                val hasHistory = events.any { it.year < year }

                if (hasHistory) {
                    // Standard count: Jan 1st to Today
                    today.dayOfYear.toLong()
                } else {
                    // "Fresh Start" count: First Event Date to Today
                    val firstEvent = yearEvents.firstOrNull() // yearEvents is already sorted
                    if (firstEvent != null) {
                        val firstDate = LocalDate.of(firstEvent.year, firstEvent.month, firstEvent.day)
                        // ChronoUnit.DAYS.between(today, today) is 0, so we add 1 to include the start day
                        ChronoUnit.DAYS.between(firstDate, today) + 1
                    } else {
                        // No events yet this year, avoid division by zero (total is 0 anyway)
                        1L
                    }
                }
            }
        }

        val average = if (daysToCount > 0) total.toDouble() / daysToCount else 0.0

        Pair(total, average)
    }.asLiveData()


    // 4. MONTHLY CHARTS (Filtered by Month & Year)
    private val monthStats = combine(allDefecationEvents, _selectedYear, _selectedMonth) { events, year, month ->
        events.filter { it.year == year && it.month == month }
    }

    private val dailyCountsFlow = monthStats.map { events ->
        events.groupingBy { it.day }.eachCount()
    }

    val dailyCounts = dailyCountsFlow.asLiveData()

    val maxDayInMonth = dailyCountsFlow.map { map ->
        if (map.isEmpty()) null else map.maxByOrNull { it.value }
    }.asLiveData()

    // 5. STREAKS (Global)
    val streakStats = allDefecationEvents.map { events ->
        calculateStreaks(events)
    }.asLiveData()

    fun setYear(year: Int) { _selectedYear.value = year }
    fun setMonth(month: Int) { _selectedMonth.value = month }

    // --- HELPER FUNCTIONS ---

    private fun calculateMode(intArray: IntArray): Int {
        if (intArray.isEmpty()) return 0

        // Group by value -> count frequency
        // e.g. [0, 2, 0, 1, 2, 0] -> {0=3, 2=2, 1=1} -> Mode is 0
        val frequencies = intArray.toTypedArray().groupingBy { it }.eachCount()

        // Return the value with the highest frequency
        // In case of a tie, we take the largest number (arbitrary choice)
        return frequencies.maxByOrNull { it.value }?.key ?: 0
    }

    private fun calculateStreaks(events: List<Event>): Pair<StreakInfo, StreakInfo> {
        if (events.isEmpty()) {
            val empty = StreakInfo(0, null, null, false)
            return Pair(empty, empty)
        }

        val dates = events.map { LocalDate.of(it.year, it.month, it.day) }
            .distinct()
            .sorted()
        var maxStreak = 0
        var maxStart: LocalDate? = null
        var maxEnd: LocalDate? = null
        var currentStreak = 1
        var currentStart = dates[0]
        for (i in 0 until dates.size - 1) {
            val d1 = dates[i]
            val d2 = dates[i + 1]

            if (ChronoUnit.DAYS.between(d1, d2) == 1L) {
                currentStreak++
            } else {
                if (currentStreak > maxStreak) {
                    maxStreak = currentStreak
                    maxStart = currentStart
                    maxEnd = d1
                }
                currentStreak = 1
                currentStart = d2
            }
        }
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak
            maxStart = currentStart
            maxEnd = dates.last()
        }

        val longest = StreakInfo(maxStreak, maxStart, maxEnd, false)

        // Current Streak
        val today = LocalDate.now()
        val diff = ChronoUnit.DAYS.between(dates.last(), today)
        val currentStreakObj: StreakInfo

        if (diff > 0L){
            currentStreakObj = StreakInfo(0, today, today, false)
        }
        else {
            // Count backwards from the last date
            var cLen = 1
            var cStart = today
            for (i in dates.size - 1 downTo 1) {
                if (ChronoUnit.DAYS.between(dates[i-1], dates[i]) == 1L) {
                    cLen++
                    cStart = dates[i-1]
                } else {
                    break
                }
            }
            currentStreakObj = StreakInfo(cLen, cStart, today, true)
        }


        return Pair(currentStreakObj, longest)
    }
}