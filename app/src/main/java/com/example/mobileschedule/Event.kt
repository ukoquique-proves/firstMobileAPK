package com.example.mobileschedule

import java.util.Date

data class Event(val date: Date, val description: String, val hour: Int = 0)
