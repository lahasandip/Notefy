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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

const val notificationId = 10

@AndroidEntryPoint
class Notifications : BroadcastReceiver() {
    @Inject
    lateinit var noteDao: NoteDao
    private var flag = false
    override fun onReceive(context: Context?, intent: Intent?) {
        val note  = intent?.getParcelableExtra<NoteEntity>("note")
        var icon : Bitmap? = null
        try {
            icon = BitmapFactory.decodeStream(
                context?.contentResolver?.openInputStream(Uri.parse(note?.image)))
            flag =true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        GlobalScope.launch {
            if (note != null) noteDao.updateDao(note.copy(isStriked = true))
        }
        val bundle = Bundle()
        bundle.putParcelable("home",note)

        val pendingIntent = context?.let {
            NavDeepLinkBuilder(it)
                .setComponentName(MainActivity::class.java)
                .setGraph(R.navigation.mobile_navigation)
                .setDestination(R.id.new_update_note)
                .setArguments(bundle)
                .createPendingIntent()
        }

        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_small_icon)
            .setContentTitle(note?.title ?: "")
            .setContentText(note?.body ?: "")
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
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}