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

    @Query("DELETE from Note where id = :iD")
     suspend fun deleteById(iD:Int)


    @Query("select * from Note where Hide = 0 order by id DESC",)
    fun getNewestToOldestData(): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 order by id ASC")
    fun getOldestToNewestData(): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 order by lower(Title) ASC ")
    fun getByTitleAscData(): Flow<List<NoteEntity>>

    @Query("select * from Note where Hide = 0 order by lower(Title) DESC ")
    fun getByTitleDscData(): Flow<List<NoteEntity>>

    @Query("select * from Note where Important = 1 and Hide = 0")
    fun getBookmarkedData(): Flow<List<NoteEntity>>

    @Query("Select * from Note where Hide = 0 and title like '%' || :query || '%'")
    fun searchDatabase(query : String) : LiveData<List<NoteEntity>>

    @Query("select * from Note where Hide = 1 order by id DESC",)
    fun getTrashData(): Flow<List<NoteEntity>>

    fun getTasks(sortOrder: SortOrder): Flow<List<NoteEntity>> =
        when (sortOrder) {
            SortOrder.BOOKMARKED -> getBookmarkedData()
            SortOrder.TITLE_ASC -> getByTitleAscData()
            SortOrder.TITLE_DSC -> getByTitleDscData()
            SortOrder.NEW_TO_OLD -> getNewestToOldestData()
            SortOrder.OLD_TO_NEW -> getOldestToNewestData()
        }

    @Query("select count(id) from Note")
    fun getNotes() : LiveData<Int>

    @Query("select count(Time) from Note where Time not like ''")
    fun getReminders() : LiveData<Int>

    @Query("select count(TodoList) from Note where TodoList not like '[]'")
    fun getTodos() : LiveData<Int>

    @Query("delete from Note where Hide in (:list)")
    fun deleteAllTrash(list : List<Int>)


}