package com.example.mobileschedule

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Date

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
        val cal1 = java.util.Calendar.getInstance().apply { time = date1 }
        val cal2 = java.util.Calendar.getInstance().apply { time = date2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
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
