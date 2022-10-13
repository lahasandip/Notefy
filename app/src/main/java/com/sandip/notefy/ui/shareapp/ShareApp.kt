package com.sandip.notefy.ui.shareapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sandip.notefy.R

class ShareApp : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.share_app))
            .setMessage(getString(R.string.share_ask))
            .setNegativeButton(getString(R.string.later), null)
            .setPositiveButton(getString(R.string.share)) { _, _ ->
                try {
                    val message = R.string.message
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, getString(R.string.share_from_notefy))
                        putExtra(Intent.EXTRA_TEXT, message)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION )
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }
                catch (e: Exception) {
                    Toast.makeText(context, getString(R.string.oops), Toast.LENGTH_LONG).show()
                }
            }
            .create()
}