package com.sandip.notefy.ui.newupdate

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.data.NoteDao
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.data.TodoDao
import com.sandip.notefy.data.TodoEntity
import com.sandip.notefy.ui.ADD_TASK_RESULT_OK
import com.sandip.notefy.ui.DELETE_TASK_RESULT_OK
import com.sandip.notefy.ui.EDIT_TASK_RESULT_OK
import com.sandip.notefy.ui.dialogs.DisplayDialogs
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NewUpdateNoteViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val todoDao: TodoDao,
    private val state: SavedStateHandle): ViewModel() {

    val note = state.get<NoteEntity>("home")


    var noteId = state.get<Int>("noteId") ?: note?.id ?: 0
        set(value) {
            field = value
            state["noteId"] = value
        }

    var noteTitle = state.get<String>("noteTitle") ?:note?.title ?: ""
        set(value) {
            field = value
            state["noteTitle"] = value
        }
    var noteDescription = state.get<String>("noteDescription") ?: note?.body ?: ""
        set(value) {
            field = value
            state["noteDescription"] = value
        }

    var noteImportance = state.get<Boolean>("noteImportance") ?: note?.important ?: false
        set(value) {
            field = value
            state["noteImportance"] = value
        }
    var noteUrl = state.get<String>("noteUrl") ?: note?.url ?: ""
        set(value) {
            field = value
            state["noteUrl"] = value
        }
    var noteDate = state.get<String>("noteDate") ?: note?.date ?: ""
        set(value) {
            field = value
            state["noteDate"] = value
        }
    var noteTime = state.get<String>("noteTime") ?: note?.time ?: ""
        set(value) {
            field = value
            state["noteTime"] = value
        }
    var noteLocation = state.get<String>("noteLocation") ?: note?.location ?: ""
        set(value) {
            field = value
            state["noteLocation"] = value
        }


    var noteColor = state.get<Int>("noteColor") ?: note?.clr ?: -1
        set(value) {
            field = value
            state["noteColor"] = value
        }
    var noteImage = state.get<Bitmap>("noteImage") ?: note?.image
        set(value) {
            field = value
            state["noteImage"] = value
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick(completed: ArrayList<Boolean>, todoDescription: ArrayList<String>) {
        if (noteTitle.isBlank()) {
            showInvalidInputMessage("Title cannot be empty")
            return
        }
        println("Bittu $noteTitle ")
        println("Bittu $noteDescription ")

        if (note != null) {
            val updatedTask = note.copy(title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                date = noteDate,
                time = noteTime,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoDescription = todoDescription,
                completed = completed
            )
            val updateTodo = TodoEntity(noteId, todoDescription, completed)
            updateTask(updatedTask,updateTodo)
        } else {
            val newTask = NoteEntity(title = noteTitle, body = noteDescription, important = noteImportance,
                url = noteUrl, date =  noteDate, time = noteTime, location =  noteLocation, clr =  noteColor, todoDescription,completed,image =  noteImage)
            val newTodo = TodoEntity(noteId+1, todoDescription, completed)
            createTask(newTask, newTodo)



        }
    }

    fun onConfirmDeleteClick() = viewModelScope.launch {
        noteDao.deleteById(noteId)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackAfterDelete(
            DELETE_TASK_RESULT_OK))
    }


    fun onBackClick() = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackScreen)
    }

    private fun createTask(noteEntity: NoteEntity, newTodo: TodoEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
        todoDao.insertDao(newTodo)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(noteEntity: NoteEntity, updateTodo: TodoEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity)
        todoDao.updateDao(updateTodo)

        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

//    fun onAddFeaturesClick() = viewModelScope.launch {
//        val dialog = DisplayDialogs(R.layout.add_features_dialog, Gravity.BOTTOM)
//        addEditTaskEventChannel.send((AddEditTaskEvent.DisplayDialog(dialog)))
//    }
//
//    fun onAddColorClick()  = viewModelScope.launch {
//        val dialog = DisplayDialogs(R.layout.add_color_dialog, Gravity.BOTTOM)
//        addEditTaskEventChannel.send((AddEditTaskEvent.DisplayDialog(dialog)))
//    }
//
//    fun onAddImageClick() = viewModelScope.launch {
//        val dialog = DisplayDialogs(R.layout.add_image_dialog, Gravity.BOTTOM)
//        addEditTaskEventChannel.send((AddEditTaskEvent.DisplayDialog(dialog)))
//    }

    fun onShareClick()  = viewModelScope.launch {
        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, noteTitle)
                putExtra(Intent.EXTRA_TITLE, "Share from Notefy:")
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            addEditTaskEventChannel.send((AddEditTaskEvent.ShareIntent(shareIntent)))
        }
        catch (e: Exception) {
            showInvalidInputMessage("Oops! cant be share")
        }
    }

    fun onLocationClick(text: CharSequence?) =viewModelScope.launch{
        val uri = "geo:0,0?q=$text"
        val gmmIntentUri =
            Uri.parse(uri)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        addEditTaskEventChannel.send((AddEditTaskEvent.StartLocationIntent(mapIntent)))

    }

    fun onAddTaskClick() = viewModelScope.launch {
            addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToTodoScreen)
        }

    fun onTaskSelected(todoEntity: TodoEntity) {

    }

//    fun addTodoData(checked: Boolean, text: Editable) {
//        completed.add(checked)
//        todoDescription.add(text.toString())
//        println(completed + "\n" + todoDescription)
//    }


    sealed class AddEditTaskEvent {
        object NavigateToBackScreen : AddEditTaskEvent()
        object NavigateToTodoScreen : AddEditTaskEvent()
        data class NavigateToBackAfterDelete(val result: Int) : AddEditTaskEvent()
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
        data class DisplayDialog(val dialog: DisplayDialogs) : AddEditTaskEvent()
        data class ShareIntent(val shareIntent: Intent) : AddEditTaskEvent()
        data class StartLocationIntent(val mapIntent: Intent) : AddEditTaskEvent()

    }

}