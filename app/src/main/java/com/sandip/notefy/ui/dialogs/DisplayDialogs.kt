package com.sandip.notefy.ui.dialogs

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.sandip.notefy.R


class DisplayDialogs(addFeaturesDialog: Int, gravity: Int): DialogFragment() {

    private val featuresDialog = addFeaturesDialog
    private val grav = gravity
    private val viewModel: DisplayDialogsViewModel by viewModels()
    private val SELECT_PICTURE = 1
    private val REQUEST_IMAGE_CAPTURE = 2

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(featuresDialog)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(grav)

        val camera: LinearLayout = dialog.findViewById(R.id.take_photo)
        val image: LinearLayout = dialog.findViewById(R.id.add_photo)
//        viewModel.sendImage(camera, image)
        camera.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }
//
//        val reminder: LinearLayout = dialog.findViewById(R.id.add_reminder_layout)
//        val place: LinearLayout = dialog.findViewById(R.id.add_place_layout)
//        val url: LinearLayout = dialog.findViewById(R.id.add_url_layout)
//        viewModel.sendAddMore(reminder,place,url)

//        viewModel.sendColor()
        return dialog
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        binding.showImage.visibility = View.VISIBLE
//
//        if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == AppCompatActivity.RESULT_OK)) {
//
//            val imageBitmap = data?.extras?.get("data") as Bitmap
////            Bitmap.createScaledBitmap(imageBitmap, 120,120,false)
//            viewModel.sendImage(imageBitmap)
//            view?.let { Glide.with(this).load(imageBitmap).into(it.findViewById(R.id.show_image))}
//
////            binding.setImage.setImageBitmap(imageBitmap)
//        } else if (requestCode == SELECT_PICTURE && resultCode == AppCompatActivity.RESULT_OK) {
//            val selectedImageUri: Uri? = data?.data
//            if (null != selectedImageUri) {
//                view?.let { Glide.with(this).load(selectedImageUri).into(it.findViewById(R.id.show_image)) }
//
////                binding.setImage.setImageURI(selectedImageUri)
//            }
//        } else {
//            view.findViewById(R.id.show_image).visibility = View.GONE
//        }
//    }
}
