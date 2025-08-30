package com.example.mobileschedule

import android.os.Bundle
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.content.Context
import android.content.Intent
import com.example.mobileschedule.databinding.ActivityMainBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calendar = Calendar.getInstance()
    private var events = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadEvents()
        updateCalendar()

        binding.prevMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }

        binding.nextMonth.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }

        binding.calendarGrid.setOnItemClickListener { _, _, position, _ ->
            val selectedDate = (binding.calendarGrid.adapter as CalendarAdapter).getItem(position)
            if (selectedDate != null) {
                val eventsForDay = events.filter { isSameDay(it.date, selectedDate) }

                if (eventsForDay.isNotEmpty()) {
                    val eventDescriptions = eventsForDay.joinToString("\n- ") { it.description }
                    AlertDialog.Builder(this)
                        .setTitle("Events for ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(selectedDate)}")
                        .setMessage("- $eventDescriptions")
                        .setPositiveButton("Add New Event") { _, _ ->
                            showAddEventDialog(selectedDate)
                        }
                        .setNegativeButton("Close", null)
                        .show()
                } else {
                    showAddEventDialog(selectedDate)
                }
            }
        }
    }

    private fun showAddEventDialog(date: Date) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Event")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("OK") { _, _ ->
            val eventDescription = input.text.toString()
            if (eventDescription.isNotEmpty()) {
                val event = Event(date, eventDescription)
                events.add(event)
                saveEvents()
                // scheduleNotification(event) // Temporarily disabled due to permission issues on modern Android
                updateCalendar()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }

        builder.show()
    }

    private fun updateCalendar() {
        val sdf = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        binding.monthYear.text = sdf.format(calendar.time)

        val days = ArrayList<Date>()
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        while (days.size < 42) {
            days.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }

        binding.calendarGrid.adapter = CalendarAdapter(this, days, events)

        // Navigation logic
        val currentMonth = Calendar.getInstance()
        binding.prevMonth.isEnabled = !isSameMonth(calendar, currentMonth)

        val twoMonthsLater = Calendar.getInstance()
        twoMonthsLater.add(Calendar.MONTH, 2)
        binding.nextMonth.isEnabled = !isSameMonth(calendar, twoMonthsLater)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        val cal1 = Calendar.getInstance().apply { time = date1 }
        val cal2 = Calendar.getInstance().apply { time = date2 }
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH)
    }

    private fun saveEvents() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = Gson()
        val json = gson.toJson(events)
        editor.putString("events", json)
        editor.apply()
    }

    private fun loadEvents() {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val gson = Gson()
        val json = sharedPreferences.getString("events", null)
        val type = object : TypeToken<MutableList<Event>>() {}.type
        if (json != null) {
            events = gson.fromJson(json, type)
        }
    }

    private fun scheduleNotification(event: Event) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        intent.putExtra("event_description", event.description)

        val uniqueId = (event.hashCode() + System.currentTimeMillis()).toInt()
                val pendingIntent = android.app.PendingIntent.getBroadcast(this, uniqueId, intent, android.app.PendingIntent.FLAG_IMMUTABLE)

        val calendar = Calendar.getInstance()
        calendar.time = event.date
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        alarmManager.setExact(android.app.AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
}
