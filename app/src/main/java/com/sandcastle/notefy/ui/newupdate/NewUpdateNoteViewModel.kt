package com.sandcastle.notefy.ui.newupdate

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.widget.ImageView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.dao.NoteDao
import com.sandcastle.notefy.data.entity.NoteEntity
import com.sandcastle.notefy.data.model.Todo
import com.sandcastle.notefy.ui.*
import com.sandcastle.notefy.util.Converters.Companion.getDateFormat
import com.sandcastle.notefy.util.Converters.Companion.getImageUri
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

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

    var isStrike = state.get<Boolean>("isStrike") ?: note?.strike ?: false
        set(value) {
            field = value
            state["isStrike"] = value
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

    fun onSaveClick(context: Context) {
        if (noteTitle.isBlank()) {
            showInvalidInputMessage(context.getString(R.string.title_cannot_be_empty))
            return
        }

        if (note != null) {
            val updatedTask = note.copy(title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                dateTime = noteDateTime,
                requestCode = requestCode,
                strike = isStrike,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoList = noteTodoList,
                isHide = noteIsHide
            )
            updateTask(updatedTask)
        } else {
            val newTask = NoteEntity(title = noteTitle, body = noteDescription, important = noteImportance,
                url = noteUrl, dateTime =  noteDateTime, requestCode = requestCode, strike = isStrike,
                location =  noteLocation, clr =  noteColor, image =  noteImage, todoList = noteTodoList,  isHide = noteIsHide)
            createTask(newTask)
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

    private fun createDeleteTask(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.insertDao(noteEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithDelete(DELETE_TASK_RESULT_OK))
    }

    private fun updateDeleteTask(noteEntity: NoteEntity) = viewModelScope.launch {
        noteDao.updateDao(noteEntity)
        addEditTaskEventChannel.send(AddEditTaskEvent.NavigateBackWithDelete(DELETE_TASK_RESULT_OK))
    }

    private fun showInvalidInputMessage(text: String) = viewModelScope.launch {
        addEditTaskEventChannel.send(AddEditTaskEvent.ShowInvalidInputMessage(text))
    }


    fun onShareClick(context: Context, image: ImageView) = viewModelScope.launch {
        if (noteTitle.isBlank()) {
            showInvalidInputMessage(context.getString(R.string.title_cannot_be_empty))
        } else {
            val desc = if (noteDescription.isNotEmpty()) "\nNote: $noteDescription," else ""
            val url = if (noteUrl.isNotEmpty()) "\nUrl: $noteUrl," else ""
            val dateTime = if (noteDateTime.isNotEmpty()) "\nReminder: ${getDateFormat(noteDateTime)}," else ""
            val location = if (noteLocation.isNotEmpty()) "\nPlace: $noteLocation," else ""
            val arrayList: ArrayList<String> = ArrayList()
            if (noteTodoList?.size != null) {
                for (s in 0 until noteTodoList?.size!!) {
                    arrayList.add(noteTodoList!![s].todoDescription.toString())
                }
            }
            val todo = if (arrayList.isNotEmpty()) "\nTodo: $arrayList" else ""

            try {
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    if (image.drawable != null) {
                        val bitmap = (image.drawable as BitmapDrawable).bitmap
                        val uri = getImageUri(
                            context,
                            context.externalCacheDir,
                            "shared_image.png",
                            bitmap
                        )
                        type = "image/*"
                        putExtra(Intent.EXTRA_STREAM, uri)
                    } else {
                        type = "text/plain"
                    }
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Title: $noteTitle,$desc$url$dateTime$location$todo"
                    )
                    putExtra(Intent.EXTRA_TITLE, context.getString(R.string.share_from_notefy))
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                addEditTaskEventChannel.send((AddEditTaskEvent.ShareIntent(shareIntent)))
            } catch (e: Exception) {
                showInvalidInputMessage(context.getString(R.string.oops))
            }
        }
    }

    fun onLocationClick(text: CharSequence?) =viewModelScope.launch{
        val uri = "geo:0,0?q=$text"
        val gmmIntentUri = Uri.parse(uri)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        addEditTaskEventChannel.send((AddEditTaskEvent.StartLocationIntent(mapIntent)))
    }

    fun onDeleteClick(context: Context) {
        if (noteTitle.isBlank()) {
            showInvalidInputMessage(context.getString(R.string.title_cannot_be_empty))
            return
        }
        callAddUpdateDB()
    }

    private fun callAddUpdateDB() = viewModelScope.launch{
        if (note != null) {
            val updatedTask = note.copy(title = noteTitle,
                body = noteDescription,
                important = noteImportance,
                url = noteUrl,
                dateTime = noteDateTime,
                requestCode = requestCode,
                strike = isStrike,
                location = noteLocation,
                clr = noteColor,
                image = noteImage,
                todoList = noteTodoList,
                isHide = noteIsHide
            )
            updateDeleteTask(updatedTask)
        } else {
            val newTask = NoteEntity(title = noteTitle, body = noteDescription, important = noteImportance,
                url = noteUrl, dateTime =  noteDateTime, requestCode = requestCode, strike = isStrike,
                location =  noteLocation, clr =  noteColor, image =  noteImage, todoList = noteTodoList,  isHide = noteIsHide)
            createDeleteTask(newTask)
        }
    }

    fun createNotificationChannel(context: Context?) = viewModelScope.launch{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    fun displaySimpleNotification(context: Context?) = viewModelScope.launch {
        val notificationIntent =
            Intent(context, Notifications::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        notificationIntent.putExtra("noteTitle", noteTitle)
        notificationIntent.putExtra("noteBody", noteDescription)
        notificationIntent.putExtra("noteImage", noteImage)
        notificationIntent.putExtra("noteRequestCode", requestCode)

        val pendingNotificationIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode!!,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            getTime(),
            pendingNotificationIntent
        )
    }

    private fun getTime(): Long {
        val items1: Array<String> = noteDateTime.split("-".toRegex()).toTypedArray()
        val items2: Array<String> = items1[3].split(":".toRegex()).toTypedArray()
        val calendar = Calendar.getInstance()
        calendar.set(items1[0].toInt(), items1[1].toInt() - 1,  items1[2].toInt(),
            items2[0].toInt(),  items2[1].toInt())
        return calendar.timeInMillis
    }

    sealed class AddEditTaskEvent {
        object NavigateToBackScreen : AddEditTaskEvent()
        data class ShowInvalidInputMessage(val msg: String) : AddEditTaskEvent()
        data class NavigateBackWithResult(val result: Int) : AddEditTaskEvent()
        data class NavigateBackWithDelete(val result: Int) : AddEditTaskEvent()
        data class ShareIntent(val shareIntent: Intent) : AddEditTaskEvent()
        data class StartLocationIntent(val mapIntent: Intent) : AddEditTaskEvent()
    }
}