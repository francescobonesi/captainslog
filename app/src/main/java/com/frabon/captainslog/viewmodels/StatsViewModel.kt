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
    val medianPerDay: Double
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
    // Calculates detailed stats for every month in the selected year
    val yearStats = combine(allDefecationEvents, _selectedYear) { events, year ->
        val yearEvents = events.filter { it.year == year }

        Month.entries.map { month ->
            val monthEvents = yearEvents.filter { it.month == month.value }
            val total = monthEvents.size

            // Calculate days in this specific month (handles leap years)
            val yearMonth = YearMonth.of(year, month)
            val daysInMonth = yearMonth.lengthOfMonth()

            // Populate an array of counts for each day (0 to 30/29/27)
            // Default to 0 for every day
            val dailyCounts = IntArray(daysInMonth) { 0 }

            monthEvents.forEach { event ->
                // event.day is 1-based, array is 0-based
                if (event.day in 1..daysInMonth) {
                    dailyCounts[event.day - 1]++
                }
            }

            // Calculate Stats
            val average = if (daysInMonth > 0) total.toDouble() / daysInMonth else 0.0
            val median = calculateMedian(dailyCounts)

            MonthlyReport(month, total, average, median)
        }
    }.asLiveData()

    // 3. OVERALL YEAR SUMMARY
    val yearSummary = combine(allDefecationEvents, _selectedYear) { events, year ->
        val yearEvents = events.filter { it.year == year }
        val total = yearEvents.size

        // Calculate denominator (days passed so far if current year, or full year if past)
        val today = LocalDate.now()
        val daysToCount = if (year == today.year) {
            today.dayOfYear // Divide only by days passed so far
        } else {
            if (YearMonth.of(year, 2).isLeapYear) 366 else 365
        }

        val average = if (daysToCount > 0) total.toDouble() / daysToCount else 0.0

        // Return Pair(Total, Average)
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

    private fun calculateMedian(intArray: IntArray): Double {
        if (intArray.isEmpty()) return 0.0
        val sorted = intArray.sorted()
        val size = sorted.size
        return if (size % 2 == 0) {
            (sorted[size / 2 - 1] + sorted[size / 2]) / 2.0
        } else {
            sorted[size / 2].toDouble()
        }
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