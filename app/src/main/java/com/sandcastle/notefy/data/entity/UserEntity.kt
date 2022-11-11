package com.sandcastle.notefy.data.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "User")
@Parcelize
data class UserEntity(
    @ColumnInfo(name = "Name") val name: String? = null,
    @ColumnInfo(name = "Email") val email: String? = null,
    @ColumnInfo(name = "Phone") val phone: String? = null,
    @ColumnInfo(name="Image") val image: String? = null,
    @PrimaryKey
    val id: Int = 0
): Parcelable