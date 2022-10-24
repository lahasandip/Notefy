package com.sandip.notefy.ui.newupdate

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.NavDeepLinkBuilder
import com.sandip.notefy.R
import com.sandip.notefy.data.dao.NoteDao
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.ui.CHANNEL_ID
import com.sandip.notefy.ui.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

const val notificationId = 10

@AndroidEntryPoint
class Notifications: BroadcastReceiver() {
    @Inject
    lateinit var noteDao: NoteDao
    private var data : NoteEntity? = null
    private var flag = false
    private lateinit var icon : Bitmap
    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        val noteTitle = intent?.getStringExtra("noteTitle")
        val noteBody =  intent?.getStringExtra("noteBody")
        val noteImage =  intent?.getStringExtra("noteImage")
        val noteRequestCode =  intent?.getIntExtra("noteRequestCode", 0)

        GlobalScope.launch {
            data = noteDao.getReminderData(noteRequestCode)
            if (data != null) {
                noteDao.updateDao(data!!.copy(strike = true))
            }
        }

        try {
            icon = BitmapFactory.decodeStream(
                context?.contentResolver?.openInputStream(Uri.parse(noteImage)))
            flag =true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val bundle = Bundle()
        bundle.putParcelable("home",data)

        val pendingIntent = context?.let {
            NavDeepLinkBuilder(it)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.new_update_note)
                .setArguments(bundle)
                .createPendingIntent()
        }

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(noteTitle ?: "")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        if(flag) {
            builder.setLargeIcon(icon)
                .setStyle(
                    NotificationCompat.BigPictureStyle()
                        .bigPicture(icon)
                        .bigLargeIcon(null)
                )
        }
        if(noteBody?.isNotEmpty() == true){
            builder.setContentText(noteBody)
        }
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}