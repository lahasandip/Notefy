package com.sandip.notefy.ui.dialogs

import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.lifecycle.ViewModel
import com.sandip.notefy.ui.home.HomeViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow


class DisplayDialogsViewModel:ViewModel() {
//    fun sendImage(imageBitmap: Bitmap) {
//        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
//    }

    private val tasksEventChannel = Channel<HomeViewModel.TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()













}