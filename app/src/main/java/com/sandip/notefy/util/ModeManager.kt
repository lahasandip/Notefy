package com.sandip.notefy.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.text.format.DateFormat
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sandip.notefy.R
import java.util.*

object ModeManager {

    fun observeUiPreferences(applicationContext: Context){
        val uiSharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
            "UI",
            Context.MODE_PRIVATE
        )
        when (uiSharedPreferences.getBoolean("darkMode", false)) {
            true -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            false -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    fun observeLanguagePreference(context: Context) {
        val languageSharedPreferences : SharedPreferences = context.getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        when (languageSharedPreferences.getInt("position", 0)) {
            0 -> updateResource(context, "en")
            1 -> updateResource(context, "hi")
            2 -> updateResource(context, "es")
            3 -> updateResource(context, "bn")
            4 -> updateResource(context, "fr")
            5 -> updateResource(context, "zh")
            6 -> updateResource(context, "ta")
            7 -> updateResource(context, "pt")
            8 -> updateResource(context, "in")
            9 -> updateResource(context, "ja")
            10 -> updateResource(context, "ru")
            11 -> updateResource(context, "te")
            12 -> updateResource(context, "mr")
            13 -> updateResource(context, "tr")
            14 -> updateResource(context, "it")
        }
    }

    private fun updateResource(context: Context, code: String) {
        val locale = Locale(code)
        Locale.setDefault(locale)
        val configuration = Configuration()
        configuration.setLocale(locale)
        context.resources?.updateConfiguration(
            configuration, context.resources?.displayMetrics
        )
    }

//    //Date & Time Picker
//    val calendar = Calendar.getInstance()
//    val hour = calendar.get(Calendar.HOUR_OF_DAY)
//    val minute = calendar.get(Calendar.MINUTE)
//    val isSystem24Hour = DateFormat.is24HourFormat()
//    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
//
//    fun getDatePicker() {
//        val datePicker
//        return datePicker: MaterialDatePicker<Long> =
//            MaterialDatePicker.Builder.datePicker()
//                .setTitleText(getString(R.string.select_date))
//                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
//                .build()
//
//    }
//    fun getTimePicker() {
//        val timePicker: MaterialTimePicker =
//            MaterialTimePicker.Builder()
//                .setTimeFormat(clockFormat)
//                .setHour(hour)
//                .setMinute(minute)
//                .setTitleText(getString(R.string.select_time))
//                .build()
//    }
}
