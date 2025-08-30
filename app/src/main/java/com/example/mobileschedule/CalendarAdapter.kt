package com.example.mobileschedule

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import java.util.Calendar
import java.util.Date

class CalendarAdapter(context: Context, private val days: List<Date>, private val events: List<Event>) : ArrayAdapter<Date>(context, 0, days) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var view = convertView
        val day = getItem(position)

        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.grid_item, parent, false)
        }

        val dateText = view!!.findViewById<TextView>(R.id.date_text)

        if (day != null) {
            val calendar = Calendar.getInstance()
            calendar.time = day
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            dateText.text = dayOfMonth.toString()

            val displayMonth = calendar.get(Calendar.MONTH)
            val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

            if (displayMonth != currentMonth) {
                dateText.setTextColor(Color.LTGRAY)
            } else {
                dateText.setTextColor(Color.BLACK)
            }

            for (event in events) {
                val eventCalendar = Calendar.getInstance()
                eventCalendar.time = event.date
                if (eventCalendar.get(Calendar.DAY_OF_YEAR) == calendar.get(Calendar.DAY_OF_YEAR) && eventCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                    view.setBackgroundColor(Color.YELLOW)
                    break
                } else {
                    view.setBackgroundColor(Color.TRANSPARENT)
                }
            }
        }

        return view
    }
}
