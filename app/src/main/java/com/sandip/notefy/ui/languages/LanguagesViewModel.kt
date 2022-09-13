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
            2 -> updateResource(context, "hi")
            3 -> updateResource(context, "bn")
            4 -> updateResource(context, "bn")
            5 -> updateResource(context, "bn")
            6 -> updateResource(context, "bn")
            7 -> updateResource(context, "bn")
            8 -> updateResource(context, "bn")
            9 -> updateResource(context, "bn")
            10 -> updateResource(context, "bn")
            11 -> updateResource(context, "bn")
            12 -> updateResource(context, "bn")
            13 -> updateResource(context, "bn")
            14 -> updateResource(context, "bn")
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