package com.sandip.notefy.ui.recycle_bin

import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
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


    fun onTaskSwiped(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.deleteDao(noteEntity)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }

    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
    }

    fun onRestored(noteEntity: NoteEntity) = viewModelScope.launch {
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

    fun onOkClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToBackScreen)
    }


    val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.contextual_action_bar, menu)
//            mode?.setTitle("Select option here");

            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
//                        R.id.share -> {
//                            // Handle share icon press
//                            true
//                        }
                R.id.delete -> {
                    // Handle delete icon press
                Log.d("Delete", "all deleted")
                    mode?.finish()

                    true
                }
//                        R.id.more -> {
//                            // Handle more item (inside overflow menu) press
//                            true
//                        }
                else -> false
            }
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
        }

    }

//

    sealed class TasksEvent {
        object NavigateToBackScreen : TasksEvent()

        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()

    }
}