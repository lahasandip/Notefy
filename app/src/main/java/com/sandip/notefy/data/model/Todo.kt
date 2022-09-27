package com.sandip.notefy.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Todo(
    var completed: Boolean?,
    val todoDescription: String?,
) : Parcelable
