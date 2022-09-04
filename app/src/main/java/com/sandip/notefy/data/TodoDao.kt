package com.sandip.notefy.data

import androidx.room.*
import com.sandip.notefy.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDao(todoEntity: TodoEntity)

    @Update
    suspend fun updateDao(todoEntity: TodoEntity)

    @Delete
    suspend fun deleteDao(todoEntity: TodoEntity)

    @Query("DELETE from Note where id = :iD")
     suspend fun deleteById(iD:Int)

    @Query("select * from Todo")
    fun getTodoList(): Flow<List<TodoEntity>>


}