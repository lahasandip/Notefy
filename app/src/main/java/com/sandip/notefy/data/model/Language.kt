package com.sandip.notefy.data.model

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

data class Language(
    var flag: Int?,
    val language: String?,
)
