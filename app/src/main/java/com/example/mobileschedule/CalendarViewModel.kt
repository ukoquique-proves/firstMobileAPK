package com.example.mobileschedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Date
import com.example.mobileschedule.DateUtils

class CalendarViewModel(private val repository: EventRepository) : ViewModel() {

    private val _events = MutableLiveData<List<Event>>().apply {
        value = repository.loadEvents()
    }
    val events: LiveData<List<Event>> = _events

    fun addEvent(event: Event) {
        val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
        currentEvents.add(event)
        _events.value = currentEvents
        repository.saveEvents(currentEvents)
    }

    fun deleteEventsForDay(date: Date) {
        val currentEvents = _events.value?.toMutableList() ?: mutableListOf()
        currentEvents.removeAll { isSameDay(it.date, date) }
        _events.value = currentEvents
        repository.saveEvents(currentEvents)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        return DateUtils.isSameDay(date1, date2)
    }
}

class CalendarViewModelFactory(private val repository: EventRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalendarViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalendarViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
