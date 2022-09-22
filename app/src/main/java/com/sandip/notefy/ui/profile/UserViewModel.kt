package com.sandip.notefy.ui.profile

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.UserDao
import com.sandip.notefy.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel@Inject constructor(
    private val userDao: UserDao,
): ViewModel() {

    val displayUser = userDao.getUser()
    val rowCount = userDao.getCount()
    var name = ""
    var email = ""
    var phone = ""
    var image = ""


    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()


    fun onSaveClick(rows: Int) {
        val userEntity = UserEntity(name = name, email = email, phone = phone, image = image)
        when(rows){
            0 ->  createTask(userEntity)
            1 ->  updateTask(userEntity)
        }
    }
    private fun createTask(userEntity: UserEntity) = viewModelScope.launch {
        userDao.insertDao(userEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult)
    }

    private fun updateTask(userEntity: UserEntity) = viewModelScope.launch {
        userDao.updateDao(userEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult)
    }

    sealed class AddEditTaskEvent {

        object NavigateBackWithResult : AddEditTaskEvent()

    }

}