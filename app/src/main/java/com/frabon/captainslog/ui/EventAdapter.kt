package com.frabon.captainslog.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.frabon.captainslog.data.Event
import com.frabon.captainslog.data.EventType
import com.frabon.captainslog.databinding.ItemEventBinding
import java.util.Calendar

class EventAdapter(private val onEventClicked: (Event) -> Unit) :
    ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        return EventViewHolder.from(parent)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position), onEventClicked)
    }

    class EventViewHolder private constructor(private val binding: ItemEventBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(event: Event, onEventClicked: (Event) -> Unit) {
            // 1. Emoji
            val emoji = when (event.type) {
                EventType.URINATION -> "💧"
                EventType.DEFECATION -> "💩"
            }
            binding.eventEmoji.text = emoji

            // 2. Notes
            // If notes are empty, we can show the Type name (e.g. "Defecation") so the row isn't blank
            binding.eventNotes.text = if (event.notes.isNullOrBlank()) {
                ""
            } else {
                event.notes
            }

            // 3. Meta Data Construction (Date - Time - Stardate)
            val day = event.day.toString().padStart(2, '0')
            val month = event.month.toString().padStart(2, '0')
            // Short year (e.g. 24)
            val yearShort = event.year.toString().takeLast(2)

            val hour = event.hour.toString().padStart(2, '0')
            val minute = event.minute.toString().padStart(2, '0')

            val stardate = calculateStardate(event)

            // Format: "25/09/24 - 14:30 - SD 12345.6"
            val metaText = "$day/$month/$yearShort - $hour:$minute - SD $stardate"

            binding.eventMeta.text = metaText

            binding.root.setOnClickListener { onEventClicked(event) }
        }

        private fun calculateStardate(event: Event): String {
            // Setup Calendar with event time
            val cal = Calendar.getInstance()
            cal.set(Calendar.YEAR, event.year)
            cal.set(Calendar.MONTH, event.month - 1) // Month is 0-indexed in Calendar
            cal.set(Calendar.DAY_OF_MONTH, event.day)
            cal.set(Calendar.HOUR_OF_DAY, event.hour)
            cal.set(Calendar.MINUTE, event.minute)

            // Formula: 1000 * (Year - 1900) + (1000 * DayOfYear / DaysInYear)
            // This is a common formula to map modern dates to TNG-era looking numbers
            val baseYear = 1900
            val yearPart = (event.year - baseYear) * 1000.0

            val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
            val daysInYear = cal.getActualMaximum(Calendar.DAY_OF_YEAR)

            val dayPart = (1000.0 * dayOfYear) / daysInYear

            // Calculate final value
            val rawStardate = yearPart + dayPart

            // Format to 1 decimal place (e.g., 124734.5)
            return String.format("%.1f", rawStardate)
        }

        companion object {
            fun from(parent: ViewGroup): EventViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemEventBinding.inflate(layoutInflater, parent, false)
                return EventViewHolder(binding)
            }
        }
    }
}

class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
    override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
        return oldItem == newItem
    }
}