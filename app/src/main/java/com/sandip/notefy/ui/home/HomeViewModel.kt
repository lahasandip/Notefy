package com.sandip.notefy.ui.home


import androidx.lifecycle.*
import com.sandip.notefy.data.NoteDao
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.ui.ADD_TASK_RESULT_OK
import com.sandip.notefy.ui.DELETE_TASK_RESULT_OK
import com.sandip.notefy.ui.EDIT_TASK_RESULT_OK
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.SortOrder
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val preferencesManager: PreferencesManager,
    private val state: SavedStateHandle
): ViewModel() {
//    val searchQuery = state.getLiveData("searchQuery", "")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        preferencesFlow
    ) { filterPreferences ->
        filterPreferences
    }.flatMapLatest { (filterPreferences) ->
        noteDao.getTasks(filterPreferences.sortOrder)
    }

    val note = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskScreen)
    }
    fun onTaskSelected(noteEntity: NoteEntity) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskScreen(noteEntity))
    }

    fun onTaskSwiped(noteEntity: NoteEntity)= viewModelScope.launch {
        noteDao.deleteDao(noteEntity)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }
    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
    }

    fun onAddEditResult(result: Int) {
        when (result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Note added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Note updated")
            DELETE_TASK_RESULT_OK -> showTaskSavedConfirmationMessage("Note deleted")

        }
    }

    private fun showTaskSavedConfirmationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfirmationMessage(text))
    }


    fun searchDatabase(query: String) : LiveData<List<NoteEntity>>    {
        return noteDao.searchDatabase(query)
    }


    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        object NavigateToDrawer : TasksEvent()

        data class NavigateToEditTaskScreen(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()



    }
}