package com.sandcastle.notefy.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.sandcastle.notefy.data.entity.UserEntity

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDao(entity: UserEntity)

    @Update
    suspend fun updateDao(entity: UserEntity)

    @Query("select * from User")
    fun getUser(): LiveData<UserEntity>

    @Query("select count() from user")
    fun getCount() : LiveData<Int>
}