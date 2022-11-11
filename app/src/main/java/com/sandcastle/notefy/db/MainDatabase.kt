package com.sandcastle.notefy.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sandcastle.notefy.data.dao.NoteDao
import com.sandcastle.notefy.data.entity.NoteEntity
import com.sandcastle.notefy.data.dao.UserDao
import com.sandcastle.notefy.data.entity.UserEntity
import com.sandcastle.notefy.util.Converters

@TypeConverters(Converters::class)
@Database(entities = [NoteEntity::class, UserEntity::class], exportSchema = false, version = 1)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getNote(): NoteDao
    abstract fun getUser(): UserDao
}