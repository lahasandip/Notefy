package com.sandip.notefy.ui.languages

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
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

//    fun saveLangPos(flag: Int) = viewModelScope.launch {
//        preferencesManager.updateLanguage(flag)
//    }

    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()
        object NavigateToHomeScreen : TasksEvent()
    }
}