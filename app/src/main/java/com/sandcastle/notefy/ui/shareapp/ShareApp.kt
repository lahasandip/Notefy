package com.sandcastle.notefy.ui.shareapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.sandcastle.notefy.BuildConfig
import com.sandcastle.notefy.R

class ShareApp : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.share_app))
            .setMessage(getString(R.string.share_ask))
            .setNegativeButton(getString(R.string.later), null)
            .setPositiveButton(getString(R.string.share)) { _, _ ->
                try {
                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TITLE, getString(R.string.share_from_notefy))
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.message,  BuildConfig.APPLICATION_ID))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION )
                    }
                    startActivity(Intent.createChooser(sendIntent, null))
                }
                catch (e: Exception) {
                    Toast.makeText(activity?.applicationContext, getString(R.string.oops), Toast.LENGTH_LONG).show()
                }
            }
            .create()
}