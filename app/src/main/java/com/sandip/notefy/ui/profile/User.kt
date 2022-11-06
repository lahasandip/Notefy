package com.sandip.notefy.ui.profile

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
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
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentUserBinding
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class User : Fragment(R.layout.fragment_user) {

    private val viewModel: UserViewModel by viewModels()
    private var binding: FragmentUserBinding? = null
    private var emailWatcher: TextWatcher? = null
    private var phoneWatcher: TextWatcher? = null
    private var builder : ImagePicker.Builder? = null
    private var rows : Int = 0
    private var note : Int = 0
    private var reminder : Int = 0
    private var todo : Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentUserBinding.bind(view)
        val imageURI = Uri.parse(
            "android.resource://" + requireContext().packageName
                    + "/" + R.drawable.img_1
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

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        val fileUri = data?.data!!
                        Glide.with(requireContext()).load(fileUri).into(binding!!.circleImageView)
                        viewModel.image = fileUri.toString()
                    }
                }
            }

        binding?.apply {
            textName.setText(viewModel.name)
            textEmail.setText(viewModel.email)
            textPhone.setText(viewModel.phone)
            context?.let { it1 ->
                Glide.with(it1).load(viewModel.image).into(circleImageView)
            }

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
                context?.let { Glide.with(it).load(circleImageView.drawable).into(profilePic) }
                dialog.show()
            }

            deletePhoto.setOnClickListener {
                context?.let { delete ->
                    Glide.with(delete).load(R.drawable.img_1).into(profilePic)
                    Glide.with(delete).load(R.drawable.img_1).into(circleImageView)
                    viewModel.image = imageURI.toString()
                }
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
                builder = ImagePicker.with(requireActivity())
                val imageDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                imageDialog.apply {
                    setContentView(R.layout.add_image_dialog)
                    show()
                    window?.attributes?.windowAnimations = R.style.DialogAnimation
                    window?.setGravity(Gravity.BOTTOM)
                    val camera: LinearLayout? = findViewById(R.id.take_photo)
                    builder?.apply {
                        crop()
                        compress(1024)
                        maxResultSize(1080, 1080)
                        camera?.setOnClickListener {
                            dismiss()
                            cameraOnly()
                            createIntent { Intent: Intent? ->
                                startForProfileImageResult.launch(Intent)
                            }
                        }

                        val image: LinearLayout? = findViewById(R.id.add_photo)
                        image?.setOnClickListener {
                            dismiss()
                            galleryOnly()
                            createIntent { Intent: Intent? ->
                                startForProfileImageResult.launch(Intent)
                            }
                        }
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
                        context?.let { it1 ->
                            Glide.with(it1).load(image).into(circleImageView)
                        }
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

    override fun onStart() {
        super.onStart()
        binding?.apply {
            emailWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

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