package com.sandcastle.notefy.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sandcastle.notefy.data.model.Todo
import kotlinx.parcelize.Parcelize
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "Note")
@Parcelize
data class NoteEntity(
    @ColumnInfo(name = "Title") val title: String? = null,
    @ColumnInfo(name = "Body") val body: String? = null,
    @ColumnInfo(name = "Important") val important: Boolean = false,
    @ColumnInfo(name = "URL") val url: String? = null,
    @ColumnInfo(name = "DateTime") val dateTime: String? = null,
    @ColumnInfo(name = "RequestCode") val requestCode: Int? = null,
    @ColumnInfo(name = "IsStrike") val strike: Boolean = false,
    @ColumnInfo(name = "Location") val location: String? = null,
    @ColumnInfo(name = "Color") val clr: Int = 0,
    @ColumnInfo(name = "Image") val image: String? = null,
    @ColumnInfo(name = "Hide") val isHide: Boolean = false,
    @ColumnInfo(name = "TodoList") var todoList: List<Todo>? = null,
    @ColumnInfo(name = "Created") val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val Id: Int = 0

): Parcelable{
    val createdDateFormatted: String
        get() = SimpleDateFormat("MMM d, h:m", Locale.getDefault()).format(created)
}