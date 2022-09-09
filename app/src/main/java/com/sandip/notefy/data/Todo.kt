package com.sandip.notefy.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Todo(
    var completed: Boolean?,
    val todoDescription: String?,
) : Parcelable
