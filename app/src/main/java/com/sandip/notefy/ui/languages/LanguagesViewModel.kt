package com.sandip.notefy.ui.languages

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.internal.ContextUtils.getActivity
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.util.PreferencesManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class LanguagesViewModel: ViewModel() {
        private val preferencesManager = PreferencesManager(context = NotefyApplication.appContext)
    val readLanguageCode  = preferencesManager.languageCode.asLiveData()
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()


    fun onOkClick()  = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }

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
//
//
//    }

//    fun savePreference(position: Int)  = viewModelScope.launch{
//
//        val sharedPreferences =  NotefyApplication.appContext.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
//        var editor = sharedPreferences.edit()
//        editor.putInt("position",position)
//        editor.commit()
//
////        val sharedPreferences2 =  NotefyApplication.appContext.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
////        var editor2 = sharedPreferences2.edit()
////        editor2.putInt("pos",position)
////        editor2.commit()
//
//
//
//    }

    fun onContinueClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToHomeScreen)
    }


    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()
        object NavigateToHomeScreen : TasksEvent()
    }




}