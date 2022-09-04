package com.sandip.notefy.ui.newupdate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput

class Action1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_action1)
//        receiveInput()
//    }
//
//    private fun receiveInput() {
//        val remoteInput =
//            RemoteInput.getResultsFromIntent(this.intent)?.getCharSequence(KEY_TEXT_REPLY)
//        var textView: TextView = findViewById(R.id.action)
//        textView.text = remoteInput
//
//        // Build a new notification, which informs the user that the system
//        // handled their interaction with the previous notification.
//        val repliedNotification = NotificationCompat.Builder(this, CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_baseline_message_24)
//            .setContentText("Replied")
//            .build()
//
//        // Issue the new notification.
//        NotificationManagerCompat.from(this).apply {
//            notify(notificationId, repliedNotification)
//        }
    }
}