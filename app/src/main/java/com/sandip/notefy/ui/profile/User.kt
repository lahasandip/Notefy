package com.sandip.notefy.ui.profile

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentUserBinding
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class User : Fragment(R.layout.fragment_user) {

    private val viewModel: UserViewModel by viewModels()
    private lateinit var binding: FragmentUserBinding
    private var rows : Int = 0
    private var note : Int = 0
    private var reminder : Int = 0
    private var todo : Int = 0


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)

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
        viewModel.displayUser.observe(viewLifecycleOwner) {
            binding.apply {

                if(it != null){
                    textName.setText(it.name)
                    textEmail.setText(it.email)
                    textPhone.setText(it.phone)
                    if(!(it.image.isNullOrEmpty())){
                        val imageUri = Uri.parse(it.image)
                        context?.let { it1 -> Glide.with(it1).load(imageUri).into(circleImageView) }
                        viewModel.image = imageUri.toString()}
                }
            }
        }
        viewModel.rowCount.observe(viewLifecycleOwner) {
            rows = it.toInt()
        }

        binding.apply {
            viewModel.noteCount.observe(viewLifecycleOwner) {
                note = it.toInt()
                viewModel.startAnimation(notesNumber, note)

            }
            viewModel.reminderCount.observe(viewLifecycleOwner) {
                reminder = it.toInt()
                viewModel.startAnimation(reminderNumber, reminder)


            }
            viewModel.todoCount.observe(viewLifecycleOwner) {
                todo = it.toInt()
                println("todo $todo")
                viewModel.startAnimation(todoNumber, todo)



            }
//            startAnimation(notesNumber, note)
//            startAnimation(reminderNumber, reminder)
//            startAnimation(todoNumber, todo)
            editName.setOnClickListener {
                textName.requestFocus(View.LAYOUT_DIRECTION_LTR)
            }
            editEmail.setOnClickListener {
                textEmail.requestFocus(View.LAYOUT_DIRECTION_LTR)
            }
            textName.setText(viewModel.name)
            textEmail.setText(viewModel.email)
            textPhone.setText(viewModel.phone)

            textName.addTextChangedListener {
                viewModel.name = it.toString()
            }
            textEmail.addTextChangedListener {
                viewModel.email = it.toString()
            }
            textPhone.addTextChangedListener {
                viewModel.phone = it.toString()
            }

            save.setOnClickListener {
                viewModel.onSaveClick(rows)
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
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
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

















            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {

                        is UserViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                            Snackbar.make(view,"Profile updated", Snackbar.LENGTH_LONG).show()
                        }
                    }.exhaustive
                }
            }

        }}





    private fun startAnimation(notesNumber: TextView, count: Int) {
        val animator = ValueAnimator.ofInt(0, count)
        when (count){
            in 0..10 -> animator.duration = 1000 // 2 seconds
            in 11..100 -> animator.duration = 2000 // 2 seconds
            else -> animator.duration = 3000 // 2 seconds
        }
        animator.addUpdateListener { animation ->
            notesNumber.text = animation.animatedValue.toString()
        }
        animator.start()
    }
}