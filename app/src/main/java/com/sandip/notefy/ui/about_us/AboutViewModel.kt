package com.sandip.notefy.ui.about_us

import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class AboutViewModel : ViewModel() {
    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onOkClick()  = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackScreen)
    }

    fun onPrivacyClick()  = viewModelScope.launch{
        val privacyIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://sites.google.com/view/privacy-policy-notefy/home"))
        addEditTaskEventChannel.send(AddEditTaskEvent.StartPrivacyIntent(privacyIntent))
    }

    sealed class AddEditTaskEvent {
        object NavigateToBackScreen : AddEditTaskEvent()
        data class StartPrivacyIntent(val privacyIntent: Intent) : AddEditTaskEvent()
    }
}