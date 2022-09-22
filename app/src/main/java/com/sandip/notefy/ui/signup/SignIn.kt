package com.sandip.notefy.ui.signup

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentSignInBinding

class SignIn : Fragment(R.layout.fragment_sign_in) {

    private val viewModel: SignInViewModel by viewModels()
    private lateinit var binding: FragmentSignInBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSignInBinding.bind(view)

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data!!
                    context?.let { Glide.with(it).load(fileUri).into(binding.circleImageView) }
                    viewModel.image = fileUri.toString()
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

        binding.apply {
            save.setOnClickListener {
                viewModel.onSaveClick()
            }


            camera.setOnClickListener {

                val with: ImagePicker.Builder? = parentFragment?.let { it1 ->
                    ImagePicker.with(
                        it1
                    )
                }
                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.add_image_dialog)
                dialog.show()
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
                val camera: LinearLayout = dialog.findViewById(R.id.take_photo)
                camera.setOnClickListener {
                    dialog.dismiss()
                    with?.crop()
                    with?.cameraOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                        null
                    }
                }
                val image: LinearLayout = dialog.findViewById(R.id.add_photo)
                image.setOnClickListener {
                    dialog.dismiss()
                    with?.crop()
                    with?.galleryOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                        null
                    }
//                viewModel.onAddImageClick()
                }
            }

        }

    }
}