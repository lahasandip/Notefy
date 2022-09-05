package com.sandip.notefy.ui

import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class MainActivityViewModel : ViewModel() {

    private val addEditTaskEventChannel = Channel<MainTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onDarkTheme() = viewModelScope.launch {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        addEditTaskEventChannel.send(MainTaskEvent.UpdateDarkUI)
    }

    fun onLightTheme() = viewModelScope.launch {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        addEditTaskEventChannel.send(MainTaskEvent.UpdateLightUI)
    }


    sealed class MainTaskEvent {
        object UpdateDarkUI : MainActivityViewModel.MainTaskEvent()
        object UpdateLightUI: MainActivityViewModel.MainTaskEvent()

    }

}