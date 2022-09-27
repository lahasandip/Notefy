package com.sandip.notefy.ui.recycle_bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.ui.profile.UserViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()
    val note = noteDao.getTrashData().asLiveData()


    fun onTaskSwiped(noteEntity: NoteEntity) = viewModelScope.launch{
        noteDao.deleteDao(noteEntity)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }
    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
    }

    fun onRestored(noteEntity: NoteEntity)= viewModelScope.launch {
//        val updatedTask = NoteEntity(title = noteEntity.title,
//            body = noteEntity.body,
//            important = noteEntity.important,
//            url = noteEntity.url,
//            date = noteEntity.date,
//            time = noteEntity.time,
//            location = noteEntity.location,
//            clr = noteEntity.clr,
//            image = noteEntity.image,
//            todoList = noteEntity.todoList,
//            isHide = false
//        )
//        noteDao.updateDao(updatedTask)
//        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }
    fun onOkClick()  = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }
    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()

        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()




    }
}