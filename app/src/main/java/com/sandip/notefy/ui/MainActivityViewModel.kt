package com.sandip.notefy.ui

import android.content.Context
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.ui.MainActivity.Companion.preferencesManager
import com.sandip.notefy.util.UiMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
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

//    fun savePreference(position: Int) = viewModelScope.launch{
//
//        preferencesManager.storeLocale(position)
//    }


//    fun onTaskSelected(context: Context?, flag: Int) = viewModelScope.launch {
//        when(flag){
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
////        preferencesManager.storeLocale(flag)
//
//    }
//
//    fun updateResource(context: Context?, code: String)  = viewModelScope.launch{
//        val locale = Locale(code)
//        Locale.setDefault(locale)
//
//        val configuration = Configuration()
//        configuration.locale = locale
//
//        context?.resources?.updateConfiguration(configuration, context.resources?.displayMetrics
//        );
//        Log.d("Locale", "language set of $code")
//
//    }
    sealed class MainTaskEvent {
        object NavigateToMainActivity : MainActivityViewModel.MainTaskEvent()

        object UpdateDarkUI : MainActivityViewModel.MainTaskEvent()
        object UpdateLightUI: MainActivityViewModel.MainTaskEvent()

    }

}