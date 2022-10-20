package com.sandip.notefy.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
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

        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.view_photo_dialog)
        val profilePic = dialog.findViewById<ImageView>(R.id.profile_pic)
        val userName = dialog.findViewById<TextView>(R.id.name_user)
        val deletePhoto = dialog.findViewById<ImageView>(R.id.delete_photo)
        val back = dialog.findViewById<ImageView>(R.id.back_screen)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
        )
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.window?.setGravity(Gravity.TOP)

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val fileUri = data?.data!!
                        context?.let { Glide.with(it).load(fileUri).into(binding.circleImageView) }
                        viewModel.image = fileUri.toString()
                    }
//                    ImagePicker.RESULT_ERROR -> {
////                        Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//                    }
//                    else -> {
////                        Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
//                    }
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
                viewModel.startAnimation(todoNumber, todo)
            }

            editName.setOnClickListener {
                textName.requestFocus()
            }
            editEmail.setOnClickListener {
                textEmail.requestFocus()
            }

            circleImageView.setOnClickListener {
                userName.text = binding.textName.text
                context?.let { Glide.with(it).load(circleImageView.drawable).into(profilePic) }
                dialog.show()
            }

            deletePhoto.setOnClickListener {
                context?.let { delete ->
                    Glide.with(delete).load(R.drawable.img_1).into(profilePic)
                    Glide.with(delete).load(R.drawable.img_1).into(circleImageView)
                    val  imageURI= Uri.parse("android.resource://" + requireContext().packageName
                            + "/" + R.drawable.img_1)
                    imageURI.toString().also { viewModel.image = it }
                }
            }
            back.setOnClickListener {
                dialog.dismiss()
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
                    ImagePicker.with(it1)
                }
                val imageDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                imageDialog.setContentView(R.layout.add_image_dialog)
                imageDialog.show()
                imageDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                imageDialog.window?.setGravity(Gravity.BOTTOM)
                val camera: LinearLayout? = imageDialog.findViewById(R.id.take_photo)
                camera?.setOnClickListener {
                    imageDialog.dismiss()
                    with?.crop()
                    with?.cameraOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
                }
                val image: LinearLayout? = imageDialog.findViewById(R.id.add_photo)
                image?.setOnClickListener {
                    imageDialog.dismiss()
                    with?.crop()
                    with?.galleryOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
                }
            }

            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is UserViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is UserViewModel.AddEditTaskEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }
        }
    }
}