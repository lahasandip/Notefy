package com.sandip.notefy.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

data class Help(
    val question: String?,
    val expandedText: String?,
    var visibility : Boolean?

    )
