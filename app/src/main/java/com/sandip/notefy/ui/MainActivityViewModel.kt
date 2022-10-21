package com.sandip.notefy.ui

import androidx.lifecycle.ViewModel
import com.sandip.notefy.data.dao.UserDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel@Inject constructor(userDao: UserDao) : ViewModel() {

    val displayUser = userDao.getUser()
}