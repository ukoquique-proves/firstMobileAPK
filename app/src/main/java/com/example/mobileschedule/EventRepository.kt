package com.example.mobileschedule

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date

class EventRepository(private val context: Context) {
    private val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadEvents(): MutableList<Event> {
        val json = sharedPreferences.getString("events", "[]")
        val type = object : TypeToken<MutableList<Event>>() {}.type
        return gson.fromJson(json, type) ?: mutableListOf()
    }

    fun saveEvents(events: List<Event>) {
        val json = gson.toJson(events)
        sharedPreferences.edit().putString("events", json).apply()
    }

    fun addEvent(event: Event) {
        val events = loadEvents()
        events.add(event)
        saveEvents(events)
    }

    fun deleteEventsForDay(date: Date) {
        val events = loadEvents()
        events.removeAll { DateUtils.isSameDay(it.date, date) }
        saveEvents(events)
    }
}
