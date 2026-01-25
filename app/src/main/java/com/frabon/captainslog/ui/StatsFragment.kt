package com.frabon.captainslog.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.frabon.captainslog.CaptainsLogApplication
import com.frabon.captainslog.R
import com.frabon.captainslog.databinding.FragmentStatsBinding
import com.frabon.captainslog.viewmodels.StatsViewModel
import com.frabon.captainslog.viewmodels.ViewModelFactory
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val statsViewModel: StatsViewModel by viewModels {
        ViewModelFactory(
            (requireActivity().application as CaptainsLogApplication).repository,
            requireContext().applicationContext
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSpinners()
        observeData()
    }

    private fun setupSpinners() {
        // Years Spinner (2020 - 2030)
        val currentYear = LocalDate.now().year
        val years = (currentYear-20..currentYear).toList()
        val yearAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.yearSpinner.adapter = yearAdapter
        binding.yearSpinner.setSelection(years.indexOf(currentYear))

        binding.yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                statsViewModel.setYear(years[pos])
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        // Months Spinner
        val months = Month.entries.map { it.getDisplayName(TextStyle.FULL, Locale.getDefault()) }
        val monthAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.monthSpinner.adapter = monthAdapter
        binding.monthSpinner.setSelection(LocalDate.now().monthValue - 1)

        binding.monthSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, pos: Int, p3: Long) {
                statsViewModel.setMonth(pos + 1)
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }
    }

    private fun observeData() {

        // A) YEAR SUMMARY
        statsViewModel.yearSummary.observe(viewLifecycleOwner) { (total, average) ->
            binding.textYearTotal.text = total.toString()
            binding.textYearAverage.text = String.format("%.2f", average)
        }

        // B) MONTHLY TABLE
        statsViewModel.yearStats.observe(viewLifecycleOwner) { reports ->
            binding.monthsTable.removeAllViews()

            // Header Row
            val headerRow = TableRow(context)
            val headers = listOf(getString(R.string.month), getString(R.string.total),
                getString(R.string.avg), getString(R.string.med))
            headers.forEach { title ->
                headerRow.addView(TextView(context).apply {
                    text = title
                    setPadding(16, 8, 16, 8)
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    gravity = android.view.Gravity.CENTER
                })
            }
            binding.monthsTable.addView(headerRow)

            // Data Rows
            reports.forEach { report ->
                // Only show rows that have data? Or all? Usually cleaner to show all for a full year report.
                // Let's show all for completeness, or if count > 0 to save space.
                // Since we want medians/avgs, showing "0" for empty months is useful info.
                if(report.totalCount > 0) {
                    val row = TableRow(context)
                    row.setPadding(0, 8, 0, 8)

                    // Month Name (Short)
                    row.addView(TextView(context).apply {
                        text = report.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                        setPadding(16, 8, 16, 8)
                    })

                    // Total
                    row.addView(TextView(context).apply {
                        text = report.totalCount.toString()
                        gravity = android.view.Gravity.CENTER
                        setPadding(16, 8, 16, 8)
                    })

                    // Average
                    row.addView(TextView(context).apply {
                        text = String.format("%.1f", report.averagePerDay)
                        gravity = android.view.Gravity.CENTER
                        setPadding(16, 8, 16, 8)
                    })

                    // Mode
                    row.addView(TextView(context).apply {
                        text = report.modePerDay.toString()
                        gravity = android.view.Gravity.CENTER
                        setPadding(16, 8, 16, 8)
                    })

                    binding.monthsTable.addView(row)
                }
            }
        }

        // B) Daily Chart
        statsViewModel.dailyCounts.observe(viewLifecycleOwner) { dailyMap ->
            drawChart(dailyMap)
        }

        // C) Max Record
        statsViewModel.maxDayInMonth.observe(viewLifecycleOwner) { entry ->
            if (entry != null) {
                binding.textMaxRecord.text =
                    getString(R.string.busiest_day_times, entry.key, entry.value)
            } else {
                binding.textMaxRecord.text = getString(R.string.no_data_for_this_month)
            }
        }

        // D & E) Streaks
        statsViewModel.streakStats.observe(viewLifecycleOwner) { (current, longest) ->
            val dtf = DateTimeFormatter.ofPattern("dd/MM/yy")

            // Current
            if (current.isActive) {
                binding.textCurrentStreak.text = getString(R.string.days, current.length)
                binding.textCurrentStreakDates.text =
                    getString(R.string.today, current.startDate?.format(dtf))
            } else {
                if (current.length > 0) {
                    binding.textCurrentStreak.text =
                        getString(R.string.last_streak_days, current.length)
                    binding.textCurrentStreakDates.text =
                        getString(
                            R.string.current_streak_dates,
                            current.startDate?.format(dtf),
                            current.endDate?.format(dtf)
                        )
                } else {
                    binding.textCurrentStreak.text = getString(R.string.no_current_streak)
                    binding.textCurrentStreakDates.text = ""
                }
            }

            // Longest
            if (longest.length > 0) {
                if (current.length == longest.length && current.isActive) {
                    binding.textLongestStreak.text = getString(R.string.current_is_longest)
                    binding.textLongestStreakDates.text = getString(R.string.keep_going)
                } else {
                    binding.textLongestStreak.text = getString(R.string.days_2, longest.length)
                    binding.textLongestStreakDates.text =
                        getString(
                            R.string.current_streak_dates_2,
                            longest.startDate?.format(dtf),
                            longest.endDate?.format(dtf)
                        )
                }
            } else {
                binding.textLongestStreak.text = getString(R.string.no_records)
            }
        }
    }

    private fun drawChart(data: Map<Int, Int>) {
        binding.chartContainer.removeAllViews()
        if (data.isEmpty()) return

        val maxVal = data.values.maxOrNull() ?: 1
        val daysInMonth = 31 // Simplified, or calculate actual days based on selected month

        for (i in 1..daysInMonth) {
            val count = data[i] ?: 0

            // Bar Layout
            val barContainer = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams =
                    LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                gravity = android.view.Gravity.BOTTOM
            }

            // The Bar itself
            val bar = View(context).apply {
                layoutParams =
                    LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
                        // Calculate height percentage
                        weight = if (count == 0) 0f else (count.toFloat() / maxVal.toFloat())
                    }
                setBackgroundColor(requireContext().getColor(R.color.brown_500))
            }

            // Text below bar (Day number) - Optional, might be too crowded
            // Let's just add tooltips or keep it simple.

            barContainer.addView(bar)

            // Empty space on top
            if (count < maxVal) {
                val space = View(context).apply {
                    layoutParams =
                        LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0).apply {
                            weight =
                                1f - (if (count == 0) 0f else (count.toFloat() / maxVal.toFloat()))
                        }
                }
                barContainer.addView(space, 0) // Add at top
            }

            binding.chartContainer.addView(barContainer)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}