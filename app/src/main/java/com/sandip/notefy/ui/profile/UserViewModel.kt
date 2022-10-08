package com.sandip.notefy.ui.profile

import android.animation.ValueAnimator
import android.widget.TextView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.data.entity.UserEntity
import com.sandip.notefy.ui.PROFILE_UPDATED_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UserViewModel@Inject constructor(
    private val userDao: UserDao,
    private val noteDao: NoteDao
): ViewModel() {

    val displayUser = userDao.getUser()
    val rowCount = userDao.getCount()
    val noteCount = noteDao.getNotes()
    val reminderCount = noteDao.getReminders()
    val todoCount = noteDao.getTodos()

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
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(PROFILE_UPDATED_RESULT_OK))
    }

    private fun updateTask(userEntity: UserEntity) = viewModelScope.launch {
        userDao.updateDao(userEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(PROFILE_UPDATED_RESULT_OK))
    }

    fun startAnimation(notesNumber: TextView, count: Int) {
        val animator = ValueAnimator.ofInt(0, count)
        when (count){
            in 0..10 -> animator.duration = 1000 // 2 seconds
            in 11..100 -> animator.duration = 2000 // 2 seconds
            else -> animator.duration = 3000 // 2 seconds
        }
        animator.addUpdateListener { animation ->
            notesNumber.text = animation.animatedValue.toString()
        }
        animator.start()
    }
    fun onOkClick()  = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackScreen)
    }
    sealed class AddEditTaskEvent {

        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
        object NavigateToBackScreen : AddEditTaskEvent()
    }
}