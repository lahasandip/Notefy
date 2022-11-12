package com.sandcastle.notefy.ui.newupdate

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
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.dao.NoteDao
import com.sandcastle.notefy.data.entity.NoteEntity
import com.sandcastle.notefy.ui.CHANNEL_ID
import com.sandcastle.notefy.ui.MainActivity
import com.sandcastle.notefy.ui.NOTIFICATION_ID
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class Notifications: BroadcastReceiver() {
    @Inject
    lateinit var noteDao: NoteDao
    private var data: NoteEntity? = null
    private var bundle: Bundle? = null
    private var flag = false
    private lateinit var icon: Bitmap

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context?, intent: Intent?) {
        val noteTitle = intent?.getStringExtra("noteTitle")
        val noteBody = intent?.getStringExtra("noteBody")
        val noteImage = intent?.getStringExtra("noteImage")
        val noteRequestCode = intent?.getIntExtra("noteRequestCode", 0)

        GlobalScope.launch {
            data = noteDao.getReminderData(noteRequestCode)
            if (data != null) {
                data?.copy(strike = true)?.let { noteDao.updateDao(it) }
            }
            bundle = Bundle()
            bundle?.putParcelable("home", data?.copy(strike = true))
            try {
                icon = BitmapFactory.decodeStream(
                    context?.contentResolver?.openInputStream(Uri.parse(noteImage))
                )
                flag = true
            } catch (_: Exception) {}

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
            if (flag) {
                builder.setLargeIcon(icon)
                    .setStyle(
                        NotificationCompat.BigPictureStyle()
                            .bigPicture(icon)
                            .bigLargeIcon(null)
                    )
            }
            if (noteBody?.isNotEmpty() == true) {
                builder.setContentText(noteBody)
            }
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
        }
    }
}