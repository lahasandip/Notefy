package com.sandip.notefy.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sandip.notefy.util.Converters


@TypeConverters(Converters::class)
@Database(entities = [NoteEntity::class, UserEntity::class], exportSchema = false, version = 1)
abstract class MainDatabase : RoomDatabase() {
    abstract fun getNote(): NoteDao
    abstract fun getUser(): UserDao

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