package com.sandip.notefy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sandip.notefy.data.entity.NoteEntity
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

    @Query("DELETE from Note where Id = :iD")
    suspend fun deleteById(iD:Int)

    @Query("select * from Note where Id = :id")
    fun getBroadcastNote(id: Int): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 and Title||Body like '%' || :query || '%' order by Id DESC",)
    fun getNewestToOldestData(query: String): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 and Title||Body like '%' || :query || '%' order by Id ASC")
    fun getOldestToNewestData(query: String): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 and Title||Body like '%' || :query || '%' order by lower(Title) ASC ")
    fun getByTitleAscData(query: String): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 and Title||Body like '%' || :query || '%' order by lower(Title) DESC ")
    fun getByTitleDscData(query: String): Flow<List<NoteEntity>>

    @Query("select * from Note where Important = 1 and Hide = 0 and Title||Body like '%' || :query || '%'")
    fun getBookmarkedData(query: String): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 1 order by Id DESC",)
    fun getTrashData(): Flow<List<NoteEntity>>

    fun getTasks(query: String, sortOrder: SortOrder): Flow<List<NoteEntity>> =
        when (sortOrder) {
            SortOrder.BOOKMARKED -> getBookmarkedData(query)
            SortOrder.TITLE_ASC -> getByTitleAscData(query)
            SortOrder.TITLE_DSC -> getByTitleDscData(query)
            SortOrder.NEW_TO_OLD -> getNewestToOldestData(query)
            SortOrder.OLD_TO_NEW -> getOldestToNewestData(query)
        }

    @Query("select count(Id) from Note")
    fun getNotes() : LiveData<Int>

    @Query("select count(DateTime) from Note where DateTime not like '' and IsStrike = 0")
    fun getReminders() : LiveData<Int>

    @Query("select count(TodoList) from Note where TodoList not like '[]'")
    fun getTodos() : LiveData<Int>

    @Query("select * from Note where RequestCode = :reqCode")
    fun getReminderData(reqCode: Int?): NoteEntity
}