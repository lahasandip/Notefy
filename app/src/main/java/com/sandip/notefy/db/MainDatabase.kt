package com.sandip.notefy.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.data.dao.UserDao
import com.sandip.notefy.data.entity.UserEntity
import com.sandip.notefy.util.Converters


@TypeConverters(Converters::class)
@Database(entities = [NoteEntity::class, UserEntity::class], exportSchema = false, version = 1)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getNote(): NoteDao
    abstract fun getUser(): UserDao
}