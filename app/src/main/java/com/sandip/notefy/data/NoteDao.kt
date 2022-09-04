package com.sandip.notefy.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sandip.notefy.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDao(entityPerson: NoteEntity)

    @Update
    suspend fun updateDao(entityPerson: NoteEntity)

    @Delete
    suspend fun deleteDao(entityPerson: NoteEntity)

    @Query("DELETE from Note where id = :iD")
     suspend fun deleteById(iD:Int)


    @Query("select * from Note order by id DESC",)
    fun getNewestToOldestData(): Flow<List<NoteEntity>>

    @Query("select * from Note order by id ASC")
    fun getOldestToNewestData(): Flow<List<NoteEntity>>

    @Query("select * from Note order by lower(Title) ASC")
    fun getByTitleAscData(): Flow<List<NoteEntity>>

    @Query("select * from Note where Important = 1")
    fun getBookmarkedData(): Flow<List<NoteEntity>>

    @Query("Select * from Note where title like '%' || :query || '%'")
    fun searchDatabase(query : String) : LiveData<List<NoteEntity>>

    fun getTasks(sortOrder: SortOrder): Flow<List<NoteEntity>> =
        when (sortOrder) {
            SortOrder.BOOKMARKED -> getBookmarkedData()
            SortOrder.TITLE_ASC -> getByTitleAscData()
            SortOrder.NEW_TO_OLD -> getNewestToOldestData()
            SortOrder.OLD_TO_NEW -> getOldestToNewestData()
        }

}