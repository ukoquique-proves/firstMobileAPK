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
import com.example.mobileschedule.DateUtils
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
                val eventsForDay = viewModel.events.value?.filter { DateUtils.isSameDay(it.date, selectedDate) } ?: emptyList()

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
            .setTitle("Action for ${DateUtils.formatFullDate(date)}")
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
        val eventDescriptions = eventsForDay.joinToString("\n- ") { event -> 
            val hourStr = if (event.hour in 0..23) event.hour.toString().padStart(2, '0') + ":00" else "00:00"
            "$hourStr - ${event.description}"
        }
        AlertDialog.Builder(this)
            .setTitle("Events for ${DateUtils.formatFullDate(date)}")
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
            .setMessage("Are you sure you want to delete all events for ${DateUtils.formatFullDate(date)}?")
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

        // Add a time picker for hour selection
        val timePicker = android.widget.TimePicker(this)
        timePicker.setIs24HourView(true)
        val linearLayout = android.widget.LinearLayout(this)
        linearLayout.orientation = android.widget.LinearLayout.VERTICAL
        linearLayout.addView(input)
        linearLayout.addView(timePicker)
        builder.setView(linearLayout)

        builder.setPositiveButton("OK") { _, _ ->
            val eventDescription = input.text.toString()
            if (eventDescription.isNotEmpty()) {
                val eventHour = timePicker.hour
                val event = Event(date, eventDescription, eventHour)
                viewModel.addEvent(event)
                // Hide the virtual keyboard
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                imm.hideSoftInputFromWindow(input.windowToken, 0)
                // Notification scheduling is temporarily disabled due to permission issues on modern Android versions.
                // scheduleNotification(event)
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> 
            // Hide the virtual keyboard on cancel as well
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(input.windowToken, 0)
            dialog.cancel()
        }

        val dialog = builder.create()
        dialog.setOnShowListener {
            input.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.showSoftInput(input, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
        }
        dialog.setOnDismissListener {
            // Ensure keyboard is hidden when dialog is dismissed
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(input.windowToken, 0)
        }
        dialog.show()
    }

    private fun updateCalendar() {
        updateMonthYearDisplay()
        val days = calculateCalendarDays()
        updateCalendarGrid(days)
        updateNavigationButtons()
    }

    private fun updateMonthYearDisplay() {
        binding.monthYear.text = DateUtils.formatMonthYear(calendar.time)
    }

    private fun calculateCalendarDays(): List<Date> {
        val days = ArrayList<Date>()
        val monthCalendar = calendar.clone() as Calendar
        monthCalendar.set(Calendar.DAY_OF_MONTH, 1)
        val firstDayOfMonth = monthCalendar.get(Calendar.DAY_OF_WEEK) - 1
        monthCalendar.add(Calendar.DAY_OF_MONTH, -firstDayOfMonth)

        while (days.size < 42) {
            days.add(monthCalendar.time)
            monthCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        return days
    }

    private fun updateCalendarGrid(days: List<Date>) {
        binding.calendarGrid.adapter = CalendarAdapter(this, days, events)
    }

    private fun updateNavigationButtons() {
        val currentMonth = Calendar.getInstance()
        binding.prevMonth.isEnabled = !DateUtils.isSameMonth(calendar, currentMonth)

        val twoMonthsLater = Calendar.getInstance()
        twoMonthsLater.add(Calendar.MONTH, 2)
        binding.nextMonth.isEnabled = !DateUtils.isSameMonth(calendar, twoMonthsLater)
    }

    private fun isSameDay(date1: Date, date2: Date): Boolean {
        return DateUtils.isSameDay(date1, date2)
    }

    private fun isSameMonth(cal1: Calendar, cal2: Calendar): Boolean {
        return DateUtils.isSameMonth(cal1, cal2)
    }

    // Notification scheduling is temporarily disabled due to permission issues on modern Android versions.
    // This function is kept for future reference when permissions are handled.
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
