package com.sandcastle.notefy.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Todo(
    var completed: Boolean?,
    var todoDescription: String?,
) : Parcelable
