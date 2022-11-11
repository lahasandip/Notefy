package com.sandcastle.notefy.ui.languages

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LanguagesViewModel(application: Application) : AndroidViewModel(application) {
    private val app = application
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()
    val languageSharedPreferences : SharedPreferences =  app.getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)

    fun onGridViewToggle(flag: Int) = viewModelScope.launch {
        val editor = languageSharedPreferences.edit()
        editor.putInt("position",flag)
        editor.apply()
    }

    fun onContinueBackClick()  = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }

    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()
    }
}