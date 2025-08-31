package com.example.mobileschedule

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EventRepository(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun loadEvents(): MutableList<Event> {
        val json = sharedPreferences.getString("events", null)
        val type = object : TypeToken<MutableList<Event>>() {}.type
        return if (json != null) {
            gson.fromJson(json, type)
        } else {
            mutableListOf()
        }
    }

    fun saveEvents(events: List<Event>) {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(events)
        editor.putString("events", json)
        editor.apply()
    }
}
