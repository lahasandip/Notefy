package com.sandip.notefy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sandip.notefy.di.ApplicationScope
import com.sandip.notefy.util.Converters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider


@TypeConverters(Converters::class)
@Database(entities = [NoteEntity::class, TodoEntity::class], version = 2)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNote(): NoteDao
    abstract fun getTodo() : TodoDao

//    class Callback @Inject constructor(
//        private val database: Provider<NoteDatabase>,
//        @ApplicationScope private val applicationScope: CoroutineScope
//    ) : RoomDatabase.Callback()
//    {
//
//        override fun onCreate(db: SupportSQLiteDatabase) {
//            super.onCreate(db)
//
//            val dao = database.get().getNote()
//            applicationScope.launch {
//                dao.insertDao(NoteEntity("Trip to Goa", "This is my first trip", false,"https://www.google.com",
//                "15 Aug, 2022","12:10", "Goa", -5185306, null))
//            }
//        }
//    }
}