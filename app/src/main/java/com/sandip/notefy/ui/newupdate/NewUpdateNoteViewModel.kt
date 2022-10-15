package com.sandip.notefy.ui.newupdate

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.data.model.Todo
import com.sandip.notefy.ui.ADD_TASK_RESULT_OK
import com.sandip.notefy.ui.EDIT_TASK_RESULT_OK
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class NewUpdateNoteViewModel @Inject constructor(
    private val noteDao: NoteDao,
    private val state: SavedStateHandle): ViewModel() {

    val note = state.get<NoteEntity>("home")

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
    var noteDateTime = state.get<String>("noteDateTime") ?: note?.dateTime ?: ""
        set(value) {
            field = value
            state["noteDateTime"] = value
        }

    var requestCode = state.get<Int>("requestCode") ?: note?.requestCode
        set(value) {
            field = value
            state["requestCode"] = value
        }

    var isStriked = state.get<Boolean>("isStriked") ?: note?.isStriked ?: false
        set(value) {
            field = value
            state["isStriked"] = value
        }

    var noteLocation = state.get<String>("noteLocation") ?: note?.location ?: ""
        set(value) {
            field = value
            state["noteLocation"] = value
        }

    var noteColor = state.get<Int>("noteColor") ?: note?.clr ?: 0
        set(value) {
            field = value
            state["noteColor"] = value
        }
    var noteImage = state.get<String>("noteImage") ?: note?.image
        set(value) {
            field = value
            state["noteImage"] = value
        }
    var noteTodoList = state.get<List<Todo>>("noteTodoList") ?: note?.todoList
        set(value) {
            field = value
            state["noteTodoList"] = value
        }
    var noteIsHide = state.get<Boolean>("noteIsHide") ?: note?.isHide ?: false
        set(value) {
            field = value
            state["noteIsHide"] = value
        }

    private val addEditTaskEventChannel = Channel<AddEditTaskEvent>()
    val addEditTaskEvent = addEditTaskEventChannel.receiveAsFlow()

    fun onSaveClick() {
        if (noteTitle.isBlank()) {
            showInvalidInputMessage(NotefyApplication.appContext.getString(R.string.title_cannot_be_empty))
            return
        }

        if (note != null) {
            val updatedTask = note.copy(title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                dateTime = noteDateTime,
                requestCode = requestCode,
                isStriked = isStriked,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoList = noteTodoList,
                isHide = noteIsHide
            )
            updateTask(updatedTask)
        } else {
            val newTask = NoteEntity(title = noteTitle, body = noteDescription, important = noteImportance,
                url = noteUrl, dateTime =  noteDateTime, requestCode = requestCode, isStriked = isStriked,
                location =  noteLocation, clr =  noteColor, image =  noteImage, todoList = noteTodoList,  isHide = noteIsHide)
            createTask(newTask)
        }
    }

    fun getNoteData(): NoteEntity {
        if (note != null) {
            Log.d("broadscast", "update called")

            return note.copy(
                title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                dateTime = noteDateTime,
                requestCode = requestCode,
                isStriked = true,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoList = noteTodoList,
                isHide = noteIsHide
            )
        } else {
            Log.d("broadscast", "update called")
            return NoteEntity(
                title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                dateTime = noteDateTime,
                requestCode = requestCode,
                isStriked = true,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoList = noteTodoList,
                isHide = noteIsHide
            )
        }
    }

    fun onBackClick() = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateToBackScreen)
    }

    private fun createTask(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(ADD_TASK_RESULT_OK))
    }

    private fun updateTask(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithResult(EDIT_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }

    fun onShareClick(image: ImageView) = viewModelScope.launch {
        val desc = if(noteDescription.isNotEmpty()) "\nNote: $noteDescription," else ""
        val url =  if(noteUrl.isNotEmpty()) "\nUrl: $noteUrl," else ""
        val dateTime =    if(noteDateTime.isNotEmpty()) "\nDate: $noteDateTime," else ""
        val location = if(noteLocation.isNotEmpty()) "\nPlace: $noteLocation," else ""
        val arrayList : ArrayList<String> = ArrayList()
        if(noteTodoList?.size != null) {
            for (s in 0 until noteTodoList?.size!!){
                arrayList.add(noteTodoList!![s].todoDescription.toString())
            }
        }
        val todo = if(arrayList.isNotEmpty()) "\nTodo: $arrayList" else ""

        try {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                if(image.drawable != null){
                    val bitmap = (image.drawable as BitmapDrawable).bitmap
                    val uri : Uri = getUri(bitmap)
                    type = "image/*"
                    putExtra(Intent.EXTRA_STREAM,uri)
                }
                else{
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_TEXT, "Title: $noteTitle,$desc$url$dateTime$location$todo")
                putExtra(Intent.EXTRA_TITLE, NotefyApplication.appContext.getString(R.string.share_from_notefy))
            }
            val shareIntent = Intent.createChooser(sendIntent, null)
            addEditTaskEventChannel.send((AddEditTaskEvent.ShareIntent(shareIntent)))
        }
        catch (e: Exception) {
            showInvalidInputMessage(NotefyApplication.appContext.getString(R.string.oops))
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

    private fun getUri(bitmap: Bitmap?): Uri {
        val imageFolder= File(NotefyApplication.appContext.externalCacheDir, "images")

        var uri: Uri? = null
        try {
            imageFolder.mkdirs()
            val file = File(imageFolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap?.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = NotefyApplication.appContext.let { FileProvider.getUriForFile(it, "com.sandip.notefy.provider", file) }
        } catch (e: Exception) {
            Toast.makeText(NotefyApplication.appContext, "" + e.message, Toast.LENGTH_LONG).show()
        }
        return uri!!
    }

    sealed class AddEditTaskEvent {
        object NavigateToBackScreen : AddEditTaskEvent()
        object NavigateToTodoScreen : AddEditTaskEvent()
        data class NavigateToBackAfterDelete(val result: Int) : AddEditTaskEvent()
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
        data class ShareIntent(val shareIntent: Intent) : AddEditTaskEvent()
        data class StartLocationIntent(val mapIntent: Intent) : AddEditTaskEvent()
    }
}