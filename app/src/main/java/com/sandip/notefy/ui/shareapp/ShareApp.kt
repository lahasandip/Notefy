package com.sandip.notefy.ui.shareapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class ShareApp : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle("Share app")
            .setMessage("If you find Notefy useful, share it with your friends")
            .setNegativeButton("Later", null)
            .setPositiveButton("Share") { _, _ ->
                try {
                    val message = "Hi there, I've found a super cool Notes app "+
                            "that can make your life easier. You can create colorful notes with "+
                            "text and photos, share notes, set reminders, create Todo list and much more. Experience the "+
                            "app from play store:\nhttps://play.google.com/store/apps/details?id=com.imangi.templerun"
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, "Share from Notefy:")
                        putExtra(Intent.EXTRA_TEXT, message)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION )
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
                catch (e: Exception) {
                    Toast.makeText(context, "Oops! cant be share", Toast.LENGTH_LONG).show()
                }
            }
            .create()
}