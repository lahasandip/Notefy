package com.sandip.notefy.ui.signup

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.UserDao
import com.sandip.notefy.data.UserEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userDao: UserDao,
    private val state: SavedStateHandle
): ViewModel() {

    val user = state.get<UserEntity>("user")

    var name = state.get<String>("name") ?: user?.name ?: ""
        set(value) {
            field = value
            state["name"] = value
        }

    var email = state.get<String>("email") ?:user?.email ?: ""
        set(value) {
            field = value
            state["email"] = value
        }
    var phone = state.get<String>("phone") ?: user?.phone ?: ""
        set(value) {
            field = value
            state["phone"] = value
        }

    var image = state.get<String>("image") ?: user?.image ?: ""
        set(value) {
            field = value
            state["image"] = value
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()


    fun onSaveClick() {

        if (user != null) {
            val updateUser = user.copy(
                name = name, email = email, phone = phone, image = image
            )
            updateTask(updateUser)
        } else {
            val createUser = UserEntity(name = name, email = email, phone = phone, image = image)
            createTask(createUser)
        }
    }
    private fun createTask(userEntity: UserEntity) = viewModelScope.launch {
        userDao.insertDao(userEntity)
        addEditTaskEventChannel.send(
            AddEditTaskEvent.NavigateBackWithResult
        )
    }

    private fun updateTask(userEntity: UserEntity) = viewModelScope.launch {
        userDao.updateDao(userEntity)
        addEditTaskEventChannel.send(
            AddEditTaskEvent.NavigateBackWithResult
        )
    }

    sealed class AddEditTaskEvent {

        object NavigateBackWithResult : AddEditTaskEvent()

    }

}