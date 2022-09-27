package com.sandip.notefy.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.ui.profile.UserViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class HelpFeedbackViewModel : ViewModel() {
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()



    fun onOkClick()  = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackScreen)
    }


    sealed class AddEditTaskEvent {
        object NavigateToBackScreen : AddEditTaskEvent()
    }

}