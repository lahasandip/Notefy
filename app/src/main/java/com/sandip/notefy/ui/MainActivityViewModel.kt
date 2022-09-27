package com.sandip.notefy.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.util.UiMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel@Inject constructor(
    userDao: UserDao
) : ViewModel() {

    private val addEditTaskEventChannel = Channel<MainTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    val displayUser = userDao.getUser()



    fun onDarkTheme() = viewModelScope.launch {
//        preferencesManager.setUiMode(UiMode.DARK)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
//        addEditTaskEventChannel.send(MainTaskEvent.UpdateDarkUI)
        MainActivity.preferencesManager.setUiMode(UiMode.DARK)
    }

    fun onLightTheme() = viewModelScope.launch {
//        preferencesManager.setUiMode(UiMode.LIGHT)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
//        addEditTaskEventChannel.send(MainTaskEvent.UpdateLightUI)
        MainActivity.preferencesManager.setUiMode(UiMode.LIGHT)

    }

    fun onScreenLockEnabled() = viewModelScope.launch {
//        MainActivity.preferencesManager.setBiometric(Biometric.ENABLE)

    }

    fun onScreenLockDisabled() = viewModelScope.launch{
//        MainActivity.preferencesManager.setBiometric(Biometric.DISABLE)
    }


    sealed class MainTaskEvent {
        object NavigateToMainActivity : MainActivityViewModel.MainTaskEvent()

        object UpdateDarkUI : MainActivityViewModel.MainTaskEvent()
        object UpdateLightUI: MainActivityViewModel.MainTaskEvent()

    }

}