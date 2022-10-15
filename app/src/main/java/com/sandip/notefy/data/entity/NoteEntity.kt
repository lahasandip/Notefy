package com.sandip.notefy.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sandip.notefy.data.model.Todo
import kotlinx.parcelize.Parcelize
import java.text.DateFormat
import java.text.SimpleDateFormat


@Entity(tableName = "Note")
@Parcelize
data class NoteEntity(
    @ColumnInfo(name = "Title") val title: String? = null,
    @ColumnInfo(name = "Body") val body: String? = null,
    @ColumnInfo(name = "Important") val important: Boolean = false,
    @ColumnInfo(name = "URL") val url: String? = null,
    @ColumnInfo(name = "DateTime") val dateTime: String? = null,
    @ColumnInfo(name = "Request Code") val requestCode: Int? = null,
    @ColumnInfo(name = "IsStrike") val isStriked: Boolean = false,
    @ColumnInfo(name = "Location") val location: String? = null,
    @ColumnInfo(name = "Color") val clr: Int = 0,
    @ColumnInfo(name="Image") val image: String? = null,
    @ColumnInfo(name="Hide") val isHide: Boolean = false,
    @ColumnInfo(name = "TodoList") var todoList: List<Todo>? = null,
    @ColumnInfo(name = "Created") val created: Long = System.currentTimeMillis(),
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0

): Parcelable{
    val createdDateFormatted: String
        get() = SimpleDateFormat("MMM d, h:m").format(created)
}