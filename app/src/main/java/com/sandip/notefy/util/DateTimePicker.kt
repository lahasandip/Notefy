package com.sandip.notefy.util

import android.app.Application
import android.os.Bundle
import android.os.PersistableBundle
import android.text.format.DateFormat
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sandip.notefy.NotefyApplication

class DateTimePicker: Application(){

    val context = NotefyApplication.appContext

        suspend fun datePicker()  {
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                // Respond to positive button click.
                datePicker.headerText
//                    binding.reminderLayout.visibility = View.VISIBLE
//            createNotificationChannel()
//             displaySimpleNotification()
            }
            datePicker.addOnNegativeButtonClickListener {
                // Respond to negative button click.
            }
            datePicker.addOnCancelListener {
                // Respond to cancel button click.
            }
            datePicker.addOnDismissListener {
                // Respond to dismiss events.
            }
        }

        suspend fun timePicker(){
            val isSystem24Hour = DateFormat.is24HourFormat(context)
            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
            val timePicker =
                MaterialTimePicker.Builder()
                    .setTimeFormat(clockFormat)
                    .setHour(12)
                    .setMinute(10)
                    .setTitleText("Select time")
                    .build()
            timePicker.addOnPositiveButtonClickListener {
                // call back code
//            "${timePicker.hour}:${timePicker.minute}".also { binding.time.text = it }
            }
            timePicker.addOnNegativeButtonClickListener {
                // call back code
            }
            timePicker.addOnCancelListener {
                // call back code
            }
            timePicker.addOnDismissListener {
                // call back code
            }
        }}