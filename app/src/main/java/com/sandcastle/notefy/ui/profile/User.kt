package com.sandcastle.notefy.ui.profile

import android.app.Dialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sandcastle.notefy.R
import com.sandcastle.notefy.databinding.FragmentUserBinding
import com.sandcastle.notefy.ui.CAMERA
import com.sandcastle.notefy.ui.GALLERY
import com.sandcastle.notefy.util.Converters.Companion.getImageUri
import com.sandcastle.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint


@Suppress("DEPRECATION")
@AndroidEntryPoint
class User : Fragment(R.layout.fragment_user) {

    private val viewModel: UserViewModel by viewModels()
    private var binding: FragmentUserBinding? = null
    private var emailWatcher: TextWatcher? = null
    private var phoneWatcher: TextWatcher? = null
    private var rows : Int = 0
    private var note : Int = 0
    private var reminder : Int = 0
    private var todo : Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentUserBinding.bind(view)
        val imageURI = Uri.parse(
            "android.resource://" + context?.packageName
                    + "/" + R.drawable.user
        )

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

        binding?.apply {
            textName.setText(viewModel.name)
            textEmail.setText(viewModel.email)
            textPhone.setText(viewModel.phone)

            circleImageView.setImageURI(Uri.parse(viewModel.image))


            textName.addTextChangedListener {
                viewModel.name = it.toString()
            }
            textEmail.addTextChangedListener {
                viewModel.email = it.toString()
            }
            textPhone.addTextChangedListener {
                viewModel.phone = it.toString()
            }

            viewModel.rowCount.observe(viewLifecycleOwner) {
                rows = it.toInt()
            }

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

            circleImageView.setOnClickListener {
                userName.text = binding?.textName?.text
                Glide.with(requireContext()).load(circleImageView.drawable).into(profilePic)
                dialog.show()
            }

            deletePhoto.setOnClickListener {
                Glide.with(requireContext()).load(R.drawable.user).into(profilePic)
                Glide.with(requireContext()).load(R.drawable.user).into(circleImageView)
                viewModel.image = imageURI.toString()
            }
            back.setOnClickListener {
                dialog.dismiss()
            }

            save.setOnClickListener {
                if (textEmail.error != null) {
                    Snackbar.make(view, getString(R.string.enter_valid_email), Snackbar.LENGTH_LONG).show()
                }
                else if(textPhone.error != null ){
                    Snackbar.make(view, getString(R.string.enter_valid_phone), Snackbar.LENGTH_LONG).show()
                }
                else {
                    viewModel.onSaveClick(rows)
                }
            }
            camera.setOnClickListener {
                val imageDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                imageDialog.apply {
                    setContentView(R.layout.add_image_dialog)
                    show()
                    window?.attributes?.windowAnimations = R.style.DialogAnimation
                    window?.setGravity(Gravity.BOTTOM)
                    val camera: LinearLayout? = findViewById(R.id.take_photo)
                    camera?.setOnClickListener {
                        dismiss()
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(intent, CAMERA)
                    }

                    val image: LinearLayout? = findViewById(R.id.add_photo)
                    image?.setOnClickListener {
                        dismiss()
                        val i = Intent().apply {
                            type = "image/*"
                            action = Intent.ACTION_OPEN_DOCUMENT
                        }
                        startActivityForResult(
                            i, GALLERY
                        )
                    }
                }
            }

            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }

            if (!viewModel.flag) {
                viewModel.apply {
                    displayUser.observe(viewLifecycleOwner) {
                        if (it != null) {
                            name = it.name.toString()
                            email = it.email.toString()
                            phone = it.phone.toString()
                            if (!(it.image.isNullOrEmpty())) {
                                val imageUri = Uri.parse(it.image)
                                image = imageUri.toString()
                            }
                        } else {
                            image = imageURI.toString()
                        }
                        textName.setText(name)
                        textEmail.setText(email)
                        textPhone.setText(phone)
                        Glide.with(requireContext()).load(image).into(circleImageView)
                        flag = true
                    }
                }
            }
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

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((requestCode == CAMERA) && (resultCode == AppCompatActivity.RESULT_OK)) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            Glide.with(this).load(imageBitmap).into(binding!!.circleImageView)
            viewModel.image = getImageUri(
                context,
                context?.filesDir,
                "${System.currentTimeMillis()}.jpeg",
                imageBitmap
            ).toString()
        } else if (requestCode == GALLERY && resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (null != selectedImageUri) {
                Glide.with(this).load(selectedImageUri).into(binding!!.circleImageView)
                viewModel.image = selectedImageUri.toString()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding?.apply {
            emailWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (textEmail.text.isNotEmpty() && !Patterns.EMAIL_ADDRESS.matcher(textEmail.text)
                            .matches()
                    ) {
                        textEmail.error = "Invalid Email"
                        save.isEnabled = false
                    } else {
                        save.isEnabled = true
                    }
                }

                override fun afterTextChanged(s: Editable) {}
            }
            textEmail.addTextChangedListener(emailWatcher)

            phoneWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (textPhone.text.isNotEmpty() && !Patterns.PHONE.matcher(textPhone.text)
                            .matches()
                    ) {
                        textPhone.error = "Invalid Phone"
                        save.isEnabled = false
                    } else {
                        save.isEnabled = true
                    }
                }
                override fun afterTextChanged(s: Editable) {}
            }
            textPhone.addTextChangedListener(phoneWatcher)
        }
    }


    override fun onStop() {
        super.onStop()
        binding?.textEmail?.removeTextChangedListener(emailWatcher)
        binding?.textPhone?.removeTextChangedListener(phoneWatcher)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}