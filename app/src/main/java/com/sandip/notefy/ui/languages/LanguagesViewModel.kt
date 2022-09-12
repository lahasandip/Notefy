package com.sandip.notefy.ui.languages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.ui.home.HomeViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class LanguagesViewModel : ViewModel() {

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    fun onOkClick()  = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToHomeScreen)
    }

    fun onTaskSelected(flag:Boolean) {

    }


    sealed class TasksEvent {
        object NavigateToHomeScreen : LanguagesViewModel.TasksEvent()
    }




}