package com.sandip.notefy.ui.newupdate

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
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
import com.sandip.notefy.NotefyApplication
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
    private lateinit var binding: FragmentNewUpdateNoteBinding
    private lateinit var date: String
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker
    private lateinit var viewColor : ColorDrawable

    private var todoList : ArrayList<Todo>? = arrayListOf()
    private val requestCode = System.currentTimeMillis().toInt()
    companion object {
        var recyclerView: RecyclerView? = null
        var todoAdapter: NewUpdateTodoAdapter? = null

        val notificationIntent =
            Intent(NotefyApplication.appContext, Notifications::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        val alarmManager =
            NotefyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        fun cancelAlarm(context: Context?, requestCode: Int?) {
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

        val startForProfileImageResult =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data
                if (resultCode == Activity.RESULT_OK) {
                    val fileUri = data?.data!!
                    binding.imageLayout.visibility = View.VISIBLE
                    context?.let { Glide.with(it).load(fileUri).into(binding.showImage) }
                    viewModel.noteImage = fileUri.toString()
                }
            }

//----------------------------------------------------------------------------------------------------------------------------------
//Color Dialog Initialization

        val colorDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        colorDialog.setContentView(R.layout.add_color_dialog)
        colorDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        colorDialog.window?.setGravity(Gravity.BOTTOM)

        val frame1: FrameLayout? = colorDialog.findViewById(R.id.frame_no_gradient)
        val image1: ImageView? = colorDialog.findViewById(R.id.no_gradient)
        image1?.setImageResource(R.drawable.ic_baseline_done_24)

        val frame2: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_1)
        val image2: ImageView? = colorDialog.findViewById(R.id.gradient_1)

        val frame3: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_2)
        val image3: ImageView? = colorDialog.findViewById(R.id.gradient_2)

        val frame4: FrameLayout? = colorDialog.findViewById(R.id.frame_gradient_3)
        val image4: ImageView? = colorDialog.findViewById(R.id.gradient_3)

        val frame5: FrameLayout? = colorDialog.findViewById(R.id.frame_gradient_4)
        val image5: ImageView? = colorDialog.findViewById(R.id.gradient_4)

        val frame6: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_5)
        val image6: ImageView? = colorDialog.findViewById(R.id.gradient_5)

        val frame7: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_6)
        val image7: ImageView? =
            colorDialog.findViewById(R.id.gradient_6)

        val frame8: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_7)
        val image8: ImageView? = colorDialog.findViewById(R.id.gradient_7)

        val frame9: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_8)
        val image9: ImageView? = colorDialog.findViewById(R.id.gradient_8)

        val frame10: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_9)
        val image10: ImageView? = colorDialog.findViewById(R.id.gradient_9)

        val frame11: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_10)
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
        recyclerView =   todoDialog.findViewById(R.id.todo_listview)
        val btn =   todoDialog.findViewById<Button>(R.id.done)

        //Date & Time Picker
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(getString(R.string.select_date))
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()

        val isSystem24Hour = DateFormat.is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText(getString(R.string.select_time))
                .build()
        datePicker.addOnPositiveButtonClickListener {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            date = format.format(utc.time)
            timePicker.show(childFragmentManager, "Time_Piker")
        }


        timePicker.addOnPositiveButtonClickListener {
            viewModel.isStriked = false
            "${timePicker.hour}:${timePicker.minute}".also {
                viewModel.noteDateTime = "$date-$it"
            }
            getDateFormat(viewModel.noteDateTime)
            binding.apply {
                newDateTime.paintFlags = 0
                newDateTime.text = getDateFormat(viewModel.noteDateTime)
                reminderParentLayout.visibility = View.VISIBLE
            }
        }

        //Bind with View model and Onclick Listener
        binding.apply {
            noteTitle.setText(viewModel.noteTitle)
            noteDescription.setText(viewModel.noteDescription)
            important.isChecked = viewModel.noteImportance
            if (viewModel.noteUrl.isNotEmpty()) {
                urlLink.text = viewModel.noteUrl
                urlParentLayout.visibility = View.VISIBLE
            }
            if (viewModel.noteDateTime.isNotEmpty()) {
                newDateTime.text =  getDateFormat(viewModel.noteDateTime)
                reminderParentLayout.visibility = View.VISIBLE
                if(viewModel.isStriked) {
                    newDateTime.paintFlags =
                        newDateTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }
            }

            if (viewModel.noteLocation.isNotEmpty()) {
                placeInput.text = viewModel.noteLocation
                locationParentLayout.visibility = View.VISIBLE
            }
            fragmentNewUpdateNote.setBackgroundColor(viewModel.noteColor)

            when(viewModel.noteColor){
                -13359 ->  { image2?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -2252579 -> {image3?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -16718218 -> {image4?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -43230 -> { image5?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -6982195 -> { image6?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -12490271 -> { image7?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -23296 -> {image8?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -38476 -> { image9?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -7650029 -> { image10?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -9404272 -> { image11?.setImageResource(R.drawable.ic_baseline_done_24)
                    image1?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
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
                todoAdapter = NewUpdateTodoAdapter(
                    context,
                    todoList
                )
                recyclerView?.setHasFixedSize(true)
                recyclerView?.layoutManager = LinearLayoutManager(context)
                recyclerView?.adapter = todoAdapter
                taskLayout.visibility = View.VISIBLE
                btn?.visibility=View.VISIBLE
            }

            if(viewModel.note?.createdDateFormatted != null){
                " ${viewModel.note?.createdDateFormatted}".also { noteEdited.text = it }
            }
            else {
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
                viewModel.isStriked = false
                viewModel.requestCode = requestCode
                viewModel.onSaveClick()
            }

            deleteNote.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.confirm_deletion))
                    .setMessage(getString(R.string.delete_note))
                    .setNegativeButton(getString(R.string.cancel), null)
                    .setPositiveButton(getString(R.string.yes)) { _, _ ->
                        viewModel.noteIsHide = true
                        viewModel.isStriked = true
                        cancelAlarm()
                        viewModel.onDeleteClick()
                    }
                    .create()
                    .show()
            }
            back.setOnClickListener {
                viewModel.onBackClick()
            }
            addFeatures.setOnClickListener {
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                dialog.setContentView(R.layout.add_features_dialog)
                dialog.show()
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
                val reminder: LinearLayout? = dialog.findViewById(R.id.add_reminder_layout)
                val place: LinearLayout? = dialog.findViewById(R.id.add_place_layout)
                val url: LinearLayout? = dialog.findViewById(R.id.add_url_layout)

                reminder?.setOnClickListener {
                    dialog.dismiss()
                    datePicker.show(childFragmentManager, "Date_Picker")
                }

                place?.setOnClickListener {
                    dialog.dismiss()
                    showAlert("place")
                }
                url?.setOnClickListener {
                    dialog.dismiss()
                    showAlert("url")
                }
            }

            reminderLayout.setOnClickListener {
                datePicker.show(childFragmentManager, "Date_Picker")
            }

            addColor.setOnClickListener {
                colorDialog.show()
            }

            addImage.setOnClickListener {
                val picker: ImagePicker.Builder? = parentFragment?.let { it1 ->
                    ImagePicker.with(
                        it1
                    )
                }

                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                dialog.setContentView(R.layout.add_image_dialog)
                dialog.show()
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
                val camera: LinearLayout ?= dialog.findViewById(R.id.take_photo)
                camera?.setOnClickListener {
                    dialog.dismiss()
                    picker?.crop()
                    picker?.cameraOnly()
                    picker?.compress(1024)
                    picker?.maxResultSize(1080, 1080)
                    picker?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
                }
                val image: LinearLayout? = dialog.findViewById(R.id.add_photo)
                image?.setOnClickListener {
                    dialog.dismiss()
                    picker?.crop()
                    picker?.galleryOnly()
                    picker?.compress(1024)
                    picker?.maxResultSize(1080, 1080)
                    picker?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
                }
            }
            locationLayout.setOnClickListener {
                viewModel.onLocationClick(placeInput.text)
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

            addToList?.setOnClickListener {
                if(!(descriptionTodo?.text.isNullOrEmpty())) {
                    todoList?.add(Todo(checkBoxTodo?.isChecked, descriptionTodo?.text.toString()))
                    todoAdapter = NewUpdateTodoAdapter(requireContext(), todoList)
                    recyclerView?.setHasFixedSize(true)
                    recyclerView?.layoutManager = LinearLayoutManager(context)
                    recyclerView?.adapter = todoAdapter
                    todoAdapter?.notifyDataSetChanged()
                }

                descriptionTodo?.setText("")
                checkBoxTodo?.isChecked = false
            }
            btn?.setOnClickListener {
                todoDialog.dismiss()
                if(!(todoList.isNullOrEmpty())){
                    binding.taskLayout.visibility = View.VISIBLE
                    viewModel.noteTodoList = todoList
                }
                else{
                    binding.taskLayout.visibility = View.GONE
                }
            }
            removeLocation.setOnClickListener {
                placeInput.text = null
                locationParentLayout.visibility = View.GONE
            }
            removeReminder.setOnClickListener {
                newDateTime.text = null
                viewModel.noteDateTime = ""
                cancelAlarm()
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
            viewColor = binding.fragmentNewUpdateNote.background as ColorDrawable

            frame1?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(0)
                viewModel.noteColor = viewColor.color
            }

            frame2?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.PaleVioletRed, null))
                viewModel.noteColor = viewColor.color
            }

            frame3?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Plum, null))
                viewModel.noteColor = viewColor.color
            }

            frame4?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.LimeGreen, null))
                viewModel.noteColor = viewColor.color
            }

            frame5?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.red, null))
                viewModel.noteColor = viewColor.color
            }

            frame6?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Bisque, null))
                viewModel.noteColor = viewColor.color
            }

            frame7?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.RoyalBlue, null))
                viewModel.noteColor = viewColor.color
            }

            frame8?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Orange, null))
                viewModel.noteColor = viewColor.color
            }

            frame9?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.HotPink, null))
                viewModel.noteColor = viewColor.color
            }

            frame10?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.SaddleBrown, null))
                viewModel.noteColor = viewColor.color
            }

            frame11?.setOnClickListener {
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
                binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.SlateGray, null))
                viewModel.noteColor = viewColor.color
            }

            colorPicker?.setOnClickListener {
                colorDialog.dismiss()
                val picker: ColorPickerDialog = ColorPickerDialog.Builder()
                    .setInitialColor(121212)
                    .setColorModel(ColorModel.HSV)
                    .setColorModelSwitchEnabled(true)
                    .setButtonOkText(android.R.string.ok)
                    .setButtonCancelText(android.R.string.cancel)
                    .onColorSelected { color: Int ->
                        binding.fragmentNewUpdateNote.setBackgroundColor(color)
                        viewModel.noteColor = viewColor.color
                    }
                    .create()
                picker.show(childFragmentManager, "color_picker")
            }

            //Updating UI with ViewModel Data100847802
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }
                        is AddEditTaskEvent.NavigateBackWithResult -> {
                            if(!(newDateTime.text.isNullOrEmpty())) {
                                viewModel.displaySimpleNotification(context)
                            }
                            binding.noteTitle.clearFocus()
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.NavigateBackWithDelete -> {
                            binding.noteTitle.clearFocus()
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.NavigateToBackAfterDelete -> {
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is AddEditTaskEvent.ShareIntent -> {
                            startActivity(event.shareIntent)
                        }
                        is AddEditTaskEvent.StartLocationIntent -> {
                            startActivity(event.mapIntent)
                        }
                        is AddEditTaskEvent.NavigateToTodoScreen -> {
                            val action =
                                NewUpdateNoteDirections.actionNewUpdateNoteToTodo()
                            findNavController().navigate(action)
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
        editText.requestFocus()
        builder?.setView(input)
        if(s=="url"){
            builder?.setTitle(getString(R.string.url))
            editText?.hint = getString(R.string.url_hint)
            builder?.setPositiveButton(
                getString(R.string.ok)
            ) {
                    _, _ ->
                if(editText?.text?.isNotEmpty() == true) {
                    binding.urlLink.text = editText.text.toString()
                    binding.urlParentLayout.visibility = View.VISIBLE
                }
                else{
                    view?.let { Snackbar.make(it, getString(R.string.please_enter_a_link), Snackbar.LENGTH_LONG).show() }
                    showAlert("url")
                }
            }
        }
        else if(s=="place"){
            builder?.setTitle(getString(R.string.place))
            editText?.hint = getString(R.string.place_hint)
            builder?.setPositiveButton(
                getString(R.string.ok)
            ) {
                    _, _ ->
                if(editText?.text?.isNotEmpty() == true) {
                    binding.placeInput.text = editText.text.toString()
                    binding.locationParentLayout.visibility = View.VISIBLE
                }
                else{
                    view?.let { Snackbar.make(it, getString(R.string.please_enter_a_place), Snackbar.LENGTH_LONG).show() }
                    showAlert("place")
                }
            }
        }
        builder?.setNegativeButton(getString(R.string.cancel)) {
                dialog, _ -> dialog.cancel()
        }
        builder?.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun cancelAlarm(){
        if(viewModel.requestCode != null) {
            cancelAlarm(context, viewModel.requestCode)
            viewModel.requestCode = null
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        todoAdapter = null
    }
}
