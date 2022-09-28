package com.sandip.notefy.ui.home


import android.content.Context
import android.content.res.Configuration
import android.util.Log
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.*
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.ui.ADD_TASK_RESULT_OK
import com.sandip.notefy.ui.DELETE_TASK_RESULT_OK
import com.sandip.notefy.ui.EDIT_TASK_RESULT_OK
import com.sandip.notefy.ui.MainActivity
import com.sandip.notefy.util.PreferencesManager
import com.sandip.notefy.util.SortOrder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val noteDao: NoteDao,
    userDao: UserDao,
    private val preferencesManager: PreferencesManager,
    state: SavedStateHandle
): ViewModel() {
    val searchQuery = state.getLiveData("searchQuery", "")
    val displayUser = userDao.getUser()

    val preferencesFlow = preferencesManager.preferencesFlow
    val isChecked = preferencesManager.isChecked

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
    val trash = noteDao.getTrashData().asLiveData()

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

    fun onTaskSwiped(noteEntity: NoteEntity, isHide: Boolean) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = isHide))
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessage(noteEntity))
    }
    fun onUndoDeleteClick(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = false))
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

    fun onAddToTrash(noteEntity: NoteEntity, isHide: Boolean) = viewModelScope.launch {
        noteDao.updateDao(noteEntity.copy(isHide = isHide))
    }
//    fun savePreference(position: Int)  = viewModelScope.launch{
//
//        preferencesManager.storeLocale(position)
//
//    }

    fun onTaskSelected(context: Context?, flag: Int) = viewModelScope.launch {
        when(flag){
            0 -> updateResource( context,"en")
            1 -> updateResource(context, "hi")
            2 -> updateResource(context, "es")
            3 -> updateResource(context, "bn")
            4 -> updateResource(context, "fr")
            5 -> updateResource(context, "zh")
            6 -> updateResource(context, "ta")
            7 -> updateResource(context, "pt")
            8 -> updateResource(context, "in")
            9 -> updateResource(context, "ja")
            10 -> updateResource(context, "ru")
            11 -> updateResource(context, "te")
            12 -> updateResource(context, "mr")
            13 -> updateResource(context, "tr")
            14 -> updateResource(context, "it")
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
        Log.d("Locale", "language set of $code")

    }


    val callback = object : ActionMode.Callback {

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            mode?.menuInflater?.inflate(R.menu.contextual_action_bar, menu)
            mode?.setTitle("Select option here");

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



    sealed class TasksEvent {
        object NavigateToAddTaskScreen : TasksEvent()
        object NavigateToDrawer : TasksEvent()
        object NavigateToUserScreen : TasksEvent()
        data class NavigateToEditTaskScreen(val noteEntity: NoteEntity) : TasksEvent()
        data class ShowTaskSavedConfirmationMessage(val msg: String) : TasksEvent()
        data class ShowUndoDeleteTaskMessage(val noteEntity: NoteEntity) : TasksEvent()



    }
}