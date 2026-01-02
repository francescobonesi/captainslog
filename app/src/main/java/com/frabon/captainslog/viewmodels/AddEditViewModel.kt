package com.frabon.captainslog.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frabon.captainslog.data.Event
import com.frabon.captainslog.data.EventRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class AddEditViewModel(
    private val repository: EventRepository
) : ViewModel() {

    fun getEventById(id: Int): Flow<Event> = repository.getEventById(id)

    fun insertEvent(event: Event): Job = viewModelScope.launch {
        repository.insertEvent(event)
    }

    fun updateEvent(event: Event): Job = viewModelScope.launch {
        repository.updateEvent(event)
    }

    fun deleteEvent(event: Event): Job = viewModelScope.launch {
        repository.deleteEvent(event)
    }
}