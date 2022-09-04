package com.sandip.notefy.data

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "Todo")
@Parcelize
data class TodoEntity(
    @PrimaryKey
    @ColumnInfo(name = "Id") val id: Int = 0,
    @ColumnInfo(name = "TodoDescription") val todoDescription: ArrayList<String>?,
    @ColumnInfo(name = "Completed") val completed: ArrayList<Boolean>?,
) : Parcelable
