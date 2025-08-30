package com.example.mobileschedule

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileschedule.databinding.ActivityNotificationBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val eventDescription = intent.getStringExtra("event_description")
        binding.eventDescriptionText.text = eventDescription

        binding.dismissButton.setOnClickListener {
            finish()
        }
    }
}
