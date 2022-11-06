package com.sandip.notefy.ui.newupdate

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.format.DateFormat
import android.util.Patterns
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Todo
import com.sandip.notefy.databinding.FragmentNewUpdateNoteBinding
import com.sandip.notefy.ui.newupdate.NewUpdateNoteViewModel.*
import com.sandip.notefy.util.Converters.Companion.getDateFormat
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog
import java.text.SimpleDateFormat
import java.util.*


@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class NewUpdateNote : Fragment(R.layout.fragment_new_update_note) {

    private val viewModel: NewUpdateNoteViewModel by viewModels()
    private var binding: FragmentNewUpdateNoteBinding? = null
    private lateinit var date: String
    private var datePicker: MaterialDatePicker<Long>? = null
    private var timePicker: MaterialTimePicker? = null
    private lateinit var viewColor : ColorDrawable
    private lateinit var todoAdapter: NewUpdateTodoAdapter
    private var todoList : ArrayList<Todo>? = arrayListOf()
    private val requestCode = System.currentTimeMillis().toInt()
    private lateinit var recyclerView: RecyclerView
    companion object {

        fun cancelAlarm(context: Context?, requestCode: Int?) {
            val notificationIntent =
                Intent(context, Notifications::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
            val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val pendingNotificationIntent: PendingIntent? =
                PendingIntent.getBroadcast(
                    context,
                    requestCode!!,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            alarmManager.cancel(pendingNotificationIntent)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNewUpdateNoteBinding.bind(view)
        viewModel.createNotificationChannel(context)

        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                datePicker?.show(childFragmentManager, "Date_Picker")
            }
            else{
                view.let {
                    Snackbar.make(
                        it,
                        getString(R.string.reminder_permission_error),
                        Snackbar.LENGTH_LONG
                    )
                        .setAnchorView(binding!!.saveNote)
                        .show()
                }
            }
        }

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data!!
                    binding?.imageLayout?.visibility = View.VISIBLE
                    context?.let { Glide.with(it).load(fileUri).into(binding!!.showImage) }
                    viewModel.noteImage = fileUri.toString()
                }
            }

//----------------------------------------------------------------------------------------------------------------------------------
//Color Dialog Initialization

        val colorDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        colorDialog.setContentView(R.layout.add_color_dialog)
        colorDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        colorDialog.window?.setGravity(Gravity.BOTTOM)

        val image1: ImageView? = colorDialog.findViewById(R.id.no_gradient)
        image1?.setImageResource(R.drawable.ic_baseline_done_24)
        val image2: ImageView? = colorDialog.findViewById(R.id.gradient_1)
        val image3: ImageView? = colorDialog.findViewById(R.id.gradient_2)
        val image4: ImageView? = colorDialog.findViewById(R.id.gradient_3)
        val image5: ImageView? = colorDialog.findViewById(R.id.gradient_4)
        val image6: ImageView? = colorDialog.findViewById(R.id.gradient_5)
        val image7: ImageView? = colorDialog.findViewById(R.id.gradient_6)
        val image8: ImageView? = colorDialog.findViewById(R.id.gradient_7)
        val image9: ImageView? = colorDialog.findViewById(R.id.gradient_8)
        val image10: ImageView? = colorDialog.findViewById(R.id.gradient_9)
        val image11: ImageView? = colorDialog.findViewById(R.id.gradient_10)

        val colorPicker: Button? = colorDialog.findViewById(R.id.color_picker)


//----------------------------------------------------------------------------------------------------------------------------------
//TodoDialog Initialization

        val todoDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        todoDialog.setContentView(R.layout.todo_listview)
        todoDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        todoDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        todoDialog.window?.setGravity(Gravity.BOTTOM)
        val addToList = todoDialog.findViewById<ImageView>(R.id.addTodo)
        val checkBoxTodo =   todoDialog.findViewById<CheckBox>(R.id.todoCheck)
        val descriptionTodo =   todoDialog.findViewById<EditText>(R.id.todoDesc)
        recyclerView = todoDialog.findViewById(R.id.todo_listview)!!
        val btn =   todoDialog.findViewById<Button>(R.id.done)

        //Bind with View model and Onclick Listener
        binding?.apply {
            noteTitle.setText(viewModel.noteTitle)
            noteDescription.setText(viewModel.noteDescription)
            important.isChecked = viewModel.noteImportance
            if (viewModel.noteUrl.isNotEmpty()) {
                urlLink.text = viewModel.noteUrl
                urlParentLayout.visibility = View.VISIBLE
            }
            if (viewModel.noteDateTime.isNotEmpty()) {
                newDateTime.text = getDateFormat(viewModel.noteDateTime)
                reminderParentLayout.visibility = View.VISIBLE
                if (viewModel.isStrike) {
                    newDateTime.paintFlags =
                        newDateTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

            if (viewModel.noteLocation.isNotEmpty()) {
                placeInput.text = viewModel.noteLocation
                locationParentLayout.visibility = View.VISIBLE
            }
            fragmentNewUpdateNote.setBackgroundColor(viewModel.noteColor)
            val picker: ColorPickerDialog = ColorPickerDialog.Builder()
                .setInitialColor(696969)
                .setColorModel(ColorModel.HSV)
                .setColorModelSwitchEnabled(true)
                .setButtonOkText(android.R.string.ok)
                .setButtonCancelText(android.R.string.cancel)
                .onColorSelected { color: Int ->
                    fragmentNewUpdateNote.setBackgroundColor(color)
                    image1?.setImageResource(0)
                    image2?.setImageResource(0)
                    image3?.setImageResource(0)
                    image4?.setImageResource(0)
                    image5?.setImageResource(0)
                    image6?.setImageResource(0)
                    image7?.setImageResource(0)
                    image8?.setImageResource(0)
                    image9?.setImageResource(0)
                    image10?.setImageResource(0)
                    image11?.setImageResource(0)
                    viewModel.noteColor = viewColor.color
                }.create()

            when (viewModel.noteColor) {
                -13359 -> {
                    image2?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -2252579 -> {
                    image3?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -16718218 -> {
                    image4?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -43230 -> {
                    image5?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -6982195 -> {
                    image6?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -12490271 -> {
                    image7?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -23296 -> {
                    image8?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -38476 -> {
                    image9?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -7650029 -> {
                    image10?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                -9404272 -> {
                    image11?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                }
                0 -> image1?.setImageResource(R.drawable.ic_baseline_done_24)
                else -> image1?.setImageResource(0)
            }

            if (viewModel.noteImage != null) {
                val imageUri = Uri.parse(viewModel.noteImage)
                context?.let { Glide.with(it).load(imageUri).into(showImage) }
                imageLayout.visibility = View.VISIBLE
            }

            if (viewModel.noteTodoList?.isEmpty() == false) {
                todoList = viewModel.noteTodoList as ArrayList<Todo>?
                todoAdapter = NewUpdateTodoAdapter(todoList)
                recyclerView.setHasFixedSize(true)
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = todoAdapter
                taskLayout.visibility = View.VISIBLE
                btn?.visibility = View.VISIBLE
            }

            if (viewModel.note?.createdDateFormatted != null) {
                " ${viewModel.note?.createdDateFormatted}".also { noteEdited.text = it }
            } else {
                noteEdited.text =
                    SimpleDateFormat(" h:mm a", Locale.getDefault())
                        .format(Date())
            }

            noteTitle.addTextChangedListener {
                viewModel.noteTitle = it.toString()
            }
            noteDescription.addTextChangedListener {
                viewModel.noteDescription = it.toString()
            }

            important.setOnCheckedChangeListener { _, isChecked ->
                viewModel.noteImportance = isChecked
            }
            urlLink.addTextChangedListener {
                viewModel.noteUrl = it.toString()
            }

            placeInput.addTextChangedListener {
                viewModel.noteLocation = it.toString()
            }

            saveNote.setOnClickListener {
                cancelAlarm()
                viewModel.isStrike = false
                viewModel.requestCode = requestCode
                context?.let { it1 -> viewModel.onSaveClick(it1) }
            }

            deleteNote.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_deletion))
                    .setMessage(getString(R.string.delete_note))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.noteIsHide = true
                        viewModel.isStrike = true
                        cancelAlarm()
                        context?.let { it1 -> viewModel.onDeleteClick(it1) }
                    }
                    .create()
                    .show()
            }
            back.setOnClickListener {
                viewModel.onBackClick()
            }

            addFeatures.setOnClickListener {
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                dialog.apply {
                    setContentView(R.layout.add_features_dialog)
                    show()
                    window?.attributes?.windowAnimations = R.style.DialogAnimation
                    window?.setGravity(Gravity.BOTTOM)
                    val reminder: LinearLayout? = findViewById(R.id.add_reminder_layout)
                    val place: LinearLayout? = findViewById(R.id.add_place_layout)
                    val url: LinearLayout? = findViewById(R.id.add_url_layout)

                    reminder?.setOnClickListener {
                        dismiss()
                        if (ContextCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.POST_NOTIFICATIONS,
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            //Date & Time Picker
                            val calendar = Calendar.getInstance()
                            val hour = calendar.get(Calendar.HOUR_OF_DAY)
                            val minute = calendar.get(Calendar.MINUTE)
                            val isSystem24Hour = DateFormat.is24HourFormat(context)
                            val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

                            datePicker = MaterialDatePicker.Builder.datePicker()
                                .setTitleText(getString(R.string.select_date))
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .build()

                            timePicker= MaterialTimePicker.Builder()
                                .setTimeFormat(clockFormat)
                                .setHour(hour)
                                .setMinute(minute)
                                .setTitleText(getString(R.string.select_time))
                                .build()

                            datePicker?.show(parentFragmentManager, "Date_Picker")

                            datePicker?.addOnPositiveButtonClickListener {
                                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                                utc.timeInMillis = it
                                val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                date = format.format(utc.time)
                                datePicker = null
                                timePicker?.show(parentFragmentManager, "Time_Piker")
                            }


                            timePicker?.addOnPositiveButtonClickListener {
                                viewModel.isStrike = false
                                "${timePicker?.hour}:${timePicker?.minute}".also {
                                    viewModel.noteDateTime = "$date-$it"
                                }
                                binding?.apply {
                                    newDateTime.paintFlags = 0
                                    newDateTime.text = getDateFormat(viewModel.noteDateTime)
                                    reminderParentLayout.visibility = View.VISIBLE
                                }
                                timePicker = null
                            }
                        } else {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                view.let {
                                    Snackbar.make(
                                        it,
                                        getString(R.string.reminder_permission_error),
                                        Snackbar.LENGTH_LONG
                                    )
                                        .setAnchorView(binding!!.saveNote)
                                        .show()
                                }
                            }
                        }
                    }

                    place?.setOnClickListener {
                        dismiss()
                        showAlert("place")
                    }
                    url?.setOnClickListener {
                        dismiss()
                        showAlert("url")
                    }
                }
            }
            newDateTime.setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    val items1: Array<String> = viewModel.noteDateTime.split("-".toRegex()).toTypedArray()
                    val items2: Array<String> = items1[3].split(":".toRegex()).toTypedArray()
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    calendar.set(items1[0].toInt(), items1[1].toInt() - 1, items1[2].toInt())
                    val isSystem24Hour = DateFormat.is24HourFormat(context)
                    val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
                    datePicker = MaterialDatePicker.Builder.datePicker()
                        .setTitleText(getString(R.string.select_date))
                        .setSelection(calendar.timeInMillis)
                        .build()

                    timePicker= MaterialTimePicker.Builder()
                        .setTimeFormat(clockFormat)
                        .setHour(items2[0].toInt())
                        .setMinute(items2[1].toInt())
                        .setTitleText(getString(R.string.select_time))
                        .build()

                    datePicker?.show(parentFragmentManager, "Date_Picker")

                    datePicker?.addOnPositiveButtonClickListener {
                        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        utc.timeInMillis = it
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        date = format.format(utc.time)
                        datePicker = null
                        timePicker?.show(parentFragmentManager, "Time_Piker")
                    }

                    timePicker?.addOnPositiveButtonClickListener {
                        viewModel.isStrike = false
                        "${timePicker?.hour}:${timePicker?.minute}".also {
                            viewModel.noteDateTime = "$date-$it"
                        }
                        binding?.apply {
                            newDateTime.paintFlags = 0
                            newDateTime.text = getDateFormat(viewModel.noteDateTime)
                            reminderParentLayout.visibility = View.VISIBLE
                        }
                        timePicker = null
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        view.let {
                            Snackbar.make(
                                it,
                                getString(R.string.reminder_permission_error),
                                Snackbar.LENGTH_LONG
                            )
                                .setAnchorView(binding!!.saveNote)
                                .show()
                        }
                    }
                }
            }

            addColor.setOnClickListener {
                colorDialog.show()
            }

            addImage.setOnClickListener {
                val imagePicker: ImagePicker.Builder? = parentFragment?.let { it1 ->
                    ImagePicker.with(
                        it1
                    )
                }

                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                dialog.apply {
                    setContentView(R.layout.add_image_dialog)
                    show()
                    window?.attributes?.windowAnimations = R.style.DialogAnimation
                    window?.setGravity(Gravity.BOTTOM)
                    val camera: LinearLayout? = findViewById(R.id.take_photo)
                    imagePicker?.apply {
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
            placeInput.setOnClickListener {
                viewModel.onLocationClick(placeInput.text)
            }
            placeInput.setOnLongClickListener {
                showAlert("place")
                true
            }
            urlLink.setOnLongClickListener {
                showAlert("url")
                true
            }
            urlLink.setOnLongClickListener {
                showAlert("url")
                true
            }

            share.setOnClickListener {
                context?.let { it1 -> viewModel.onShareClick(it1, showImage) }
            }

            addTask.setOnClickListener {
                todoDialog.show()
            }

            taskLayout.setOnClickListener {
                todoDialog.show()
            }
            taskLayout.setOnLongClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_deletion))
                    .setMessage(getString(R.string.delete_todo))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        todoList?.clear()
                        taskLayout.visibility = View.GONE
                    }
                    .create()
                    .show()
                true
            }

            addToList?.setOnClickListener {
                if (!(descriptionTodo?.text.isNullOrEmpty())) {
                    todoList?.add(
                        Todo(
                            checkBoxTodo?.isChecked,
                            descriptionTodo?.text.toString()
                        )
                    )
                    todoAdapter = NewUpdateTodoAdapter(todoList)
                    recyclerView.setHasFixedSize(true)
                    recyclerView.layoutManager = LinearLayoutManager(context)
                    recyclerView.adapter = todoAdapter
                    todoAdapter.notifyDataSetChanged()
                }

                descriptionTodo?.setText("")
                checkBoxTodo?.isChecked = false
            }
            btn?.setOnClickListener {
                todoDialog.dismiss()
                if (!(todoList.isNullOrEmpty())) {
                    taskLayout.visibility = View.VISIBLE
                    viewModel.noteTodoList = todoList
                } else {
                    taskLayout.visibility = View.GONE
                }
            }
            removeLocation.setOnClickListener {
                placeInput.text = null
                locationParentLayout.visibility = View.GONE
            }
            removeReminder.setOnClickListener {
                newDateTime.text = null
                viewModel.noteDateTime = ""
                reminderParentLayout.visibility = View.GONE
            }
            removeUrl.setOnClickListener {
                urlLink.text = null
                urlParentLayout.visibility = View.GONE
            }
            removeImage.setOnClickListener {
                context?.let { Glide.with(it).clear(showImage) }
                viewModel.noteImage = null
                imageLayout.visibility = View.GONE
            }
            viewColor = fragmentNewUpdateNote.background as ColorDrawable

            colorDialog.findViewById<FrameLayout>(R.id.frame_no_gradient)?.setOnClickListener {
                image1?.setImageResource(R.drawable.ic_baseline_done_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(0)
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_1)?.setOnClickListener {
                image2?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.PaleVioletRed,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_2)?.setOnClickListener {
                image3?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Plum, null))
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_3)?.setOnClickListener {
                image4?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.LimeGreen,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_4)?.setOnClickListener {
                image5?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.red, null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_5)?.setOnClickListener {
                image6?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.Bisque,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_6)?.setOnClickListener {
                image7?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.RoyalBlue,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_7)?.setOnClickListener {
                image8?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.Orange,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_8)?.setOnClickListener {
                image9?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image10?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.HotPink,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_9)?.setOnClickListener {
                image10?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image11?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.SaddleBrown,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorDialog.findViewById<FrameLayout>(R.id.frame_gradient_10)?.setOnClickListener {
                image11?.setImageResource(R.drawable.ic_baseline_done_24)
                image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                image2?.setImageResource(0)
                image3?.setImageResource(0)
                image4?.setImageResource(0)
                image5?.setImageResource(0)
                image6?.setImageResource(0)
                image7?.setImageResource(0)
                image8?.setImageResource(0)
                image9?.setImageResource(0)
                image10?.setImageResource(0)
                fragmentNewUpdateNote.setBackgroundColor(
                    resources.getColor(
                        R.color.SlateGray,
                        null
                    )
                )
                viewModel.noteColor = viewColor.color
            }

            colorPicker?.setOnClickListener {
                colorDialog.dismiss()
                picker.show(childFragmentManager, "color_picker")
            }

            //Updating UI with ViewModel Data100847802
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG)
                                .setAnchorView(saveNote)
                                .show()
                        }
                        is AddEditTaskEvent.NavigateBackWithResult -> {
                            if (!(newDateTime.text.isNullOrEmpty())) {
                                viewModel.displaySimpleNotification(context)
                            }
                            noteTitle.clearFocus()
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.NavigateBackWithDelete -> {
                            noteTitle.clearFocus()
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.ShareIntent -> {
                            startActivity(event.shareIntent)
                        }
                        is AddEditTaskEvent.StartLocationIntent -> {
                            startActivity(event.mapIntent)
                        }
                    }.exhaustive
                }

            }
        }
    }

    //Private function to display Place and URL
    private fun showAlert(s: String) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val input = layoutInflater.inflate(R.layout.alert_edittext, null)
        val editText = input.findViewById<EditText>(R.id.input)
        builder?.setView(input)
        editText.apply {
            if (s == "url") {
                builder?.setTitle(getString(R.string.url))
                if (viewModel.noteUrl.isNotEmpty()) {
                    setText(viewModel.noteUrl)
                    requestFocus()
                } else {
                    hint = getString(R.string.url_hint)
                    requestFocus()
                }
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (text.isNotEmpty() && !Patterns.WEB_URL.matcher(text.toString()).matches()) {
                            error = "Invalid\n\t\tUrl"
                        }
                        if (text.isNullOrEmpty()) {
                            hint = getString(R.string.url_hint)
                        }
                    }
                    override fun afterTextChanged(s: Editable) {
                        builder?.create()?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = true
                    }
                })

                builder?.setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    if (text?.isNotEmpty() == true) {
                        binding?.urlLink?.text = text.toString()
                        binding?.urlParentLayout?.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_a_link),
                            Toast.LENGTH_LONG
                        ).show()
                        showAlert("url")
                    }
                }
            } else if (s == "place") {
                builder?.setTitle(getString(R.string.place))
                if (viewModel.noteLocation.isNotEmpty()) {
                    setText(viewModel.noteLocation)
                    requestFocus()
                } else {
                    hint = getString(R.string.place_hint)
                    requestFocus()
                }
                addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        if (text.isNullOrEmpty()) {
                            hint = getString(R.string.place_hint)
                        }
                    }
                    override fun afterTextChanged(s: Editable) {}
                })

                builder?.setPositiveButton(
                    getString(R.string.ok)
                ) { _, _ ->
                    if (text?.isNotEmpty() == true) {
                        binding?.placeInput?.text = text.toString()
                        binding?.locationParentLayout?.visibility = View.VISIBLE
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.please_enter_a_place),
                            Toast.LENGTH_LONG
                        ).show()
                        showAlert("place")
                    }
                }
            }
        }
        builder?.setNegativeButton(getString(R.string.cancel)) { dialog, _ ->
            dialog.cancel()
        }
        builder?.show()
    }

    private fun cancelAlarm(){
        if(viewModel.requestCode != null) {
            cancelAlarm(context, viewModel.requestCode)
            viewModel.requestCode = null
        }
    }

//    override fun onStart() {
//        super.onStart()
//        //Date & Time Picker
//        val calendar = Calendar.getInstance()
//        val hour = calendar.get(Calendar.HOUR_OF_DAY)
//        val minute = calendar.get(Calendar.MINUTE)
//        datePicker =
//            MaterialDatePicker.Builder.datePicker()
//                .setTitleText(getString(R.string.select_date))
//                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
//                .build()
//
//        val isSystem24Hour = DateFormat.is24HourFormat(context)
//        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H
//
//        timePicker =
//            MaterialTimePicker.Builder()
//                .setTimeFormat(clockFormat)
//                .setHour(hour)
//                .setMinute(minute)
//                .setTitleText(getString(R.string.select_time))
//                .build()
//        datePicker?.addOnPositiveButtonClickListener {
//            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//            utc.timeInMillis = it
//            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//            date = format.format(utc.time)
//            datePicker = null
//            timePicker?.show(childFragmentManager, "Time_Piker")
//        }
//
//
//        timePicker?.addOnPositiveButtonClickListener {
//            viewModel.isStrike = false
//            "${timePicker?.hour}:${timePicker?.minute}".also {
//                viewModel.noteDateTime = "$date-$it"
//            }
//            binding?.apply {
//                newDateTime.paintFlags = 0
//                newDateTime.text = getDateFormat(viewModel.noteDateTime)
//                reminderParentLayout.visibility = View.VISIBLE
//            }
//            timePicker = null
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}
