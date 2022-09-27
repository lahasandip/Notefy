package com.sandip.notefy.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sandip.notefy.data.model.Todo
import kotlinx.parcelize.Parcelize


@Entity(tableName = "Note")
@Parcelize
data class NoteEntity(
    @ColumnInfo(name = "Title") val title: String? = null,
    @ColumnInfo(name = "Body") val body: String? = null,
    @ColumnInfo(name = "Important") val important: Boolean = false,
    @ColumnInfo(name = "URL") val url: String? = null,
    @ColumnInfo(name = "Date") val date: String? = null,
    @ColumnInfo(name = "Time") val time: String? = null,
    @ColumnInfo(name = "Location") val location: String? = null,
    @ColumnInfo(name = "Color") val clr: Int = 0,
    @ColumnInfo(name="Image") val image: String? = null,
    @ColumnInfo(name="Hide") val isHide: Boolean = false,
    @ColumnInfo(name = "TodoList") var todoList: List<Todo>? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0

): Parcelable