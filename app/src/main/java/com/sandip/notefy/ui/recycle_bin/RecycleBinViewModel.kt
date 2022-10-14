package com.sandip.notefy.ui.recycle_bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecycleBinViewModel @Inject constructor(
    private val noteDao: NoteDao
) : ViewModel() {

    val noteRestored = NotefyApplication.appContext.getString(R.string.note_restored)
    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()
    val note = noteDao.getTrashData().asLiveData()


    fun onTaskSwiped(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.deleteDao(noteEntity)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }

    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
    }

    fun onMenuRestore(noteEntity: NoteEntity, isHide: Boolean) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = isHide))
        tasksEventChannel.send(TasksEvent.ShowDeletedTaskMessage(noteRestored))
    }

    fun onOkClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }

    fun onMenuDelete(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.deleteDao(noteEntity)
    }

    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()

        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowDeletedTaskMessage(val s: String) : TasksEvent()

    }
}