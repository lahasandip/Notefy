package com.sandip.notefy.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import java.util.*

object ModeManager {

    fun observeUiPreferences(applicationContext: Context){
        val uiSharedPreferences: SharedPreferences = applicationContext.getSharedPreferences(
            "UI",
            Context.MODE_PRIVATE
        )
        Log.d("TAG", uiSharedPreferences.getBoolean("darkMode", false).toString() )

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
}
