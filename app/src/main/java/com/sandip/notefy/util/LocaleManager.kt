package com.sandip.notefy.util

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import com.sandip.notefy.NotefyApplication
import java.util.*

class LocaleManager {

    companion object {
        val languageSharedPreferences : SharedPreferences = NotefyApplication.appContext.getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        val editor: SharedPreferences.Editor = languageSharedPreferences.edit()

        fun observeLanguagePreference(context: Context) {
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
}
