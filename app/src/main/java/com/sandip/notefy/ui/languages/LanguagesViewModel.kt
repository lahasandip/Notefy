package com.sandip.notefy.ui.languages

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LanguagesViewModel  @Inject constructor (private val preferencesManager: PreferencesManager): ViewModel() {
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()
//    val langPosition = preferencesManager.langPosition


    fun onOkClick()  = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }

    fun onContinueClick() = viewModelScope.launch {
//        val sharedPreferences =  getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
//        val editor = sharedPreferences?.edit()
//        editor?.putInt("position",flag)
//        editor?.apply()
        tasksEventChannel.send(TasksEvent.NavigateToHomeScreen)
    }

//    fun observeLanguagePreference(context: Context?) = viewModelScope.launch {
//        val sharedPreferences =  context?.getSharedPreferences("LANGUAGE",Context.MODE_PRIVATE)
//        when(sharedPreferences?.getInt("position", 0)){
//            0 -> updateResource( context,"en")
//            1 -> updateResource(context, "hi")
//            2 -> updateResource(context, "es")
//            3 -> updateResource(context, "bn")
//            4 -> updateResource(context, "fr")
//            5 -> updateResource(context, "zh")
//            6 -> updateResource(context, "ta")
//            7 -> updateResource(context, "pt")
//            8 -> updateResource(context, "in")
//            9 -> updateResource(context, "ja")
//            10 -> updateResource(context, "ru")
//            11 -> updateResource(context, "te")
//            12 -> updateResource(context, "mr")
//            13 -> updateResource(context, "tr")
//            14 -> updateResource(context, "it")
//        }
//    }
//
//    private fun updateResource(context: Context?, code: String)  = viewModelScope.launch{
//        val locale = Locale(code)
//        Locale.setDefault(locale)
//        val configuration = Configuration()
//        configuration.locale = locale
//        context?.resources?.updateConfiguration(configuration, context.resources?.displayMetrics
//        )
//        Log.d("Language", "language called $code " )
//    }


    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()
        object NavigateToHomeScreen : TasksEvent()
    }
}