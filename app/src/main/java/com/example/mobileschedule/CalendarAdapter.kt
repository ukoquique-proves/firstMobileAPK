package com.example.mobileschedule

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.mobileschedule.databinding.GridItemBinding
import java.util.Calendar
import java.util.Date

class CalendarAdapter(
    context: Context,
    private val days: List<Date>,
    private val events: List<Event>
) : ArrayAdapter<Date>(context, 0, days) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: GridItemBinding
        val view: View

        if (convertView == null) {
            binding = GridItemBinding.inflate(inflater, parent, false)
            view = binding.root
            view.tag = binding
        } else {
            binding = convertView.tag as GridItemBinding
            view = convertView
        }

        val day = getItem(position)
        if (day != null) {
            val calendar = Calendar.getInstance()
            calendar.time = day
            binding.dateText.text = calendar.get(Calendar.DAY_OF_MONTH).toString()

            val today = Calendar.getInstance()
            val isCurrentMonth = calendar.get(Calendar.MONTH) == today.get(Calendar.MONTH) && calendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)
            binding.dateText.setTextColor(if (isCurrentMonth) Color.BLACK else Color.GRAY)

            val eventsForDay = events.filter { DateUtils.isSameDay(it.date, day) }
            if (eventsForDay.isNotEmpty()) {
                val color = getEventHighlightColor(eventsForDay)
                view.setBackgroundColor(color)
            } else {
                view.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        return view
    }

    private fun getEventHighlightColor(eventsForDay: List<Event>): Int {
        val today = Calendar.getInstance()
        val twoDaysFromNow = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 2) }
        val twoWeeksFromNow = Calendar.getInstance().apply { add(Calendar.WEEK_OF_YEAR, 2) }

        val hasEventWithinTwoDays = eventsForDay.any { event ->
            val eventDate = Calendar.getInstance().apply { time = event.date }
            eventDate.after(today) && eventDate.before(twoDaysFromNow)
        }

        val hasEventWithinTwoWeeks = eventsForDay.any { event ->
            val eventDate = Calendar.getInstance().apply { time = event.date }
            eventDate.after(today) && eventDate.before(twoWeeksFromNow)
        }

        return when {
            hasEventWithinTwoDays -> Color.RED
            hasEventWithinTwoWeeks -> Color.parseColor("#FFA500") // Orange
            else -> Color.YELLOW
        }
    }
}
