package com.sandip.notefy.data

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sandip.notefy.util.SortOrder
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDao(entity: UserEntity)

    @Update
    suspend fun updateDao(entity: UserEntity)
}