package com.sandip.notefy.ui.newupdate


import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.sandip.notefy.R
import com.sandip.notefy.ui.MainActivity
import com.sandip.notefy.ui.home.Home


const val notificationId = 10
const val KEY_TEXT_REPLY = "text"


class Notifications : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        val title = intent?.getStringExtra("titleExtra") ?: ""
        val desc = intent?.getStringExtra("messageExtra") ?: ""
//        val image = intent?.getByteArrayExtra("imageExtra") ?: ""

        val icon = BitmapFactory.decodeResource(context?.resources, R.drawable.image)


//        //Add a reply
//        var replyLabel = "Reply"
//        var remoteInput: RemoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).run {
//            setLabel(replyLabel)
//            build()
//        }

        //Add an action to open a new Activity
        var intent2 = Intent(context, MainActivity::class.java)
        intent2.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent: PendingIntent =
            PendingIntent.getActivity(context, 0, intent2, PendingIntent.FLAG_IMMUTABLE)
//
//        val pendingIntent2: PendingIntent =
//            PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
//        var action1: NotificationCompat.Action =
//            NotificationCompat.Action.Builder(0, "Dismiss", pendingIntent2)
//                .build()


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
//            .addAction(action1)
        with(NotificationManagerCompat.from(context)) {
            // notificationId is a unique int for each notification that you must define
            notify(notificationId, builder.build())
        }
    }
}