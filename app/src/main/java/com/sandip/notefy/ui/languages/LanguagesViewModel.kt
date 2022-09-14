package com.sandip.notefy.ui.languages

import android.content.Context
import android.content.res.Configuration
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.NotefyApplication
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*


class LanguagesViewModel : ViewModel() {
//    private val preferencesManager = PreferencesManager(context = NotefyApplication.appContext)
//    val readLanguageCode  = preferencesManager.languageCode.asLiveData()
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    fun onOkClick()  = viewModelScope.launch {


        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }

    fun onTaskSelected(context: Context?, flag: Int) = viewModelScope.launch {
        when(flag){
            0 -> updateResource( context,"en")
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
//        preferencesManager.storeLocale(flag)

    }

    fun updateResource(context: Context?, code: String)  = viewModelScope.launch{
        val locale = Locale(code)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale

       context?.resources?.updateConfiguration(configuration, context.resources?.displayMetrics
        );


    }


    sealed class TasksEvent {
        object NavigateToBackScreen : LanguagesViewModel.TasksEvent()
    }




}