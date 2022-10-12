package com.sandip.notefy.ui.newupdate

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sandip.notefy.R
import com.sandip.notefy.ui.MainActivity
import com.sandip.notefy.ui.home.NoteAdapter
import java.io.FileNotFoundException


const val notificationId = 10

class Notifications  : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {

        val bundle = intent?.extras
        val title = bundle?.getString("titleExtra") ?: ""
        val desc = bundle?.getString("messageExtra") ?: ""
        val image = bundle?.getString("imageExtra") ?: ""
        var icon : Bitmap? = null
        try {
            icon = BitmapFactory.decodeStream(
                context?.contentResolver?.openInputStream(Uri.parse(image))
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        //Add an action to open a new Activity
        val intent2 = Intent(context, MainActivity::class.java)
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(context!!, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_small_icon)
            .setContentTitle(title)
            .setContentText(desc)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setLargeIcon(icon)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(icon)
                    .bigLargeIcon(null)
            )
        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }
}