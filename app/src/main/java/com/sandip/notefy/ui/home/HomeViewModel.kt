package com.sandip.notefy.ui.home

import android.content.Context
import androidx.lifecycle.*
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.ui.ADD_TASK_RESULT_OK
import com.sandip.notefy.ui.DELETE_TASK_RESULT_OK
import com.sandip.notefy.ui.EDIT_TASK_RESULT_OK
import com.sandip.notefy.ui.PROFILE_UPDATED_RESULT_OK
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteDao: NoteDao,
    userDao: UserDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
): ViewModel() {

    companion object{
        var mutableLiveData = MutableLiveData<String?>()
    }

    val searchQuery = state.getLiveData("searchQuery", "")
    val displayUser = userDao.getUser()

    private val preferencesFlow = preferencesManager.preferencesFlow
    val isChecked = preferencesManager.isChecked

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val tasksFlow = combine(
        searchQuery.asFlow(),
        preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        noteDao.getTasks(query, filterPreferences.sortOrder)
    }

    val note = tasksFlow.asLiveData()
    val noteCount = noteDao.getNotes()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }
    fun updateSortOrderIsChecked(index:Int) = viewModelScope.launch {
        preferencesManager.updateSortOrderIsChecked(index)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }
    fun onProfileClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToUserScreen)
    }

    fun onTaskSelected(noteEntity: NoteEntity) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(noteEntity))
    }
    fun onMenuTaskDelete(noteEntity: NoteEntity, isHide: Boolean) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = isHide))
    }
    
    fun onTaskSwiped(noteEntity: NoteEntity, isHide: Boolean) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = isHide))
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }

    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = false))
    }

    fun onAddEditResult(context: Context, result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_added))
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_updated))
            DELETE_TASK_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.note_deleted))
            PROFILE_UPDATED_RESULT_OK -> showTaskSavedConfirmationMessage(context.getString(R.string.profile_updated))
        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }

    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        object NavigateToDrawer : TasksEvent()
        object NavigateToUserScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowDeletedTaskMessage(val text: String) : TasksEvent()
    }
}