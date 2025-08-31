package com.example.mobileschedule

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.GridView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileschedule.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val calendar = Calendar.getInstance()
    private var events = listOf<Event>()
    private val viewModel: CalendarViewModel by viewModels { 
        CalendarViewModelFactory(EventRepository(this))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupDayOfWeekGrid()

        viewModel.events.observe(this) {
            events = it
            updateCalendar()
        }

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
                val eventsForDay = viewModel.events.value?.filter { isSameDay(it.date, selectedDate) } ?: emptyList()

                if (eventsForDay.isNotEmpty()) {
                    showActionMenuDialog(selectedDate, eventsForDay)
                } else {
                    showAddEventDialog(selectedDate)
                }
            }
        }
    }

    private fun setupDayOfWeekGrid() {
        val daysOfWeek = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val dayOfWeekAdapter = ArrayAdapter(this, R.layout.day_of_week_item, daysOfWeek)
        binding.dayOfWeekGrid.adapter = dayOfWeekAdapter
    }

    private fun showActionMenuDialog(date: Date, eventsForDay: List<Event>) {
        val options = arrayOf("View Event(s)", "Delete Event(s)")

        AlertDialog.Builder(this)
            .setTitle("Action for ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)}")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showViewEventDialog(date, eventsForDay)
                    1 -> showDeleteConfirmationDialog(date)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showViewEventDialog(date: Date, eventsForDay: List<Event>) {
        val eventDescriptions = eventsForDay.joinToString("\n- ") { it.description }
        AlertDialog.Builder(this)
            .setTitle("Events for ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)}")
            .setMessage("- $eventDescriptions")
            .setPositiveButton("Add New Event") { _, _ ->
                showAddEventDialog(date)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showDeleteConfirmationDialog(date: Date) {
        AlertDialog.Builder(this)
            .setTitle("Delete Events")
            .setMessage("Are you sure you want to delete all events for ${SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).format(date)}?")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEventsForDay(date)
            }
            .setNegativeButton("Cancel", null)
            .show()
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
                viewModel.addEvent(event)
                // scheduleNotification(event) // Temporarily disabled due to permission issues on modern Android
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
