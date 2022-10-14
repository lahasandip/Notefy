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
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog
import java.text.SimpleDateFormat
import java.util.*

const val CHANNEL_ID: String = "4"
const val CHANNEL_NAME: String = "Notefy"
const val CHANNEL_DESCRIPTION = "Reminder Message"
@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class NewUpdateNote : Fragment(R.layout.fragment_new_update_note) {

    private val viewModel: NewUpdateNoteViewModel by viewModels()
    private lateinit var binding: FragmentNewUpdateNoteBinding
    private lateinit var date: String
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker
    private var todoList : ArrayList<Todo>? = arrayListOf()
    private val requestCode = System.currentTimeMillis().toInt()

    //Companion Object for Temp List
    companion object {
        var recyclerView: RecyclerView? = null
        var todoAdapter: NewUpdateTodoAdapter? = null

        val notificationIntent = Intent(NotefyApplication.appContext, Notifications::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val alarmManager = NotefyApplication.appContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentNewUpdateNoteBinding.bind(view)
        createNotificationChannel()
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
//                else if (resultCode == ImagePicker.RESULT_ERROR) {
////                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
//                } else {
////                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
//                }
            }

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
            // Respond to positive button click.
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it
            val format = SimpleDateFormat("yyyy-MM-dd")
            date = format.format(utc.time)
//            viewModel.noteDateTime = date

//            var date2 = viewModel.noteDateTime
//            var spf = SimpleDateFormat("yyyy-MM-dd")
//            val newDate = spf.parse(date2)
//            spf = SimpleDateFormat("MMM d, ''yy")
//            date2 = spf.format(newDate)
//            println(date2)


//            val format2=SimpleDateFormat("MMM d, ''yy")
//            val date2 = format2.format(utc.time)


//            binding.newDateTime.text = date2
            timePicker.show(childFragmentManager, "Time_Piker")

        }
        datePicker.addOnNegativeButtonClickListener {
            // Respond to negative button click.
        }
        datePicker.addOnCancelListener {
            // Respond to cancel button click.
        }
        datePicker.addOnDismissListener {
            // Respond to dismiss events.
        }


        timePicker.addOnPositiveButtonClickListener {
            // call back code

            if(viewModel.noteTitle.isNotEmpty()) {
                viewModel.isStriked = false
                "${timePicker.hour}:${timePicker.minute}".also {
                    viewModel.noteDateTime = "$date-$it"
                    var date2 = viewModel.noteDateTime
                    var spf = SimpleDateFormat("yyyy-MM-dd-h:m")
                    val newDate = spf.parse(date2)
                    spf = SimpleDateFormat("MMM d, ''yy, h:m")
                    date2 = newDate?.let { it1 -> spf.format(it1) }.toString()
                    binding.newDateTime.text = date2}
                binding.reminderParentLayout.visibility = View.VISIBLE
            }
        }
        timePicker.addOnNegativeButtonClickListener {
            // call back code
        }
        timePicker.addOnCancelListener {
            // call back code
        }
        timePicker.addOnDismissListener {
            // call back code
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
                var date = viewModel.noteDateTime
                var spf = SimpleDateFormat("yyyy-MM-dd-h:m")
                val newDate = spf.parse(date)
                spf = SimpleDateFormat("MMM d, ''yy, h:m")
                date = newDate?.let { spf.format(it) }.toString()
                newDateTime.text = date
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
                todoAdapter = context?.let {
                    NewUpdateTodoAdapter(
                        it,
                        todoList,
                    )
                }
                recyclerView?.setHasFixedSize(true)
                recyclerView?.layoutManager = LinearLayoutManager(context)
                recyclerView?.adapter = todoAdapter
                taskLayout.visibility = View.VISIBLE
                btn?.visibility=View.VISIBLE
            }

            if(viewModel.note?.createdDateFormatted != null){
                val da = viewModel.note?.createdDateFormatted
                val items1: Array<String> =
                    da?.split(" ".toRegex())?.toTypedArray() ?: arrayOf("")
                val tim = items1[3]
                val t: Array<String> =
                    tim.split(":".toRegex()).toTypedArray()
                noteEdited.text = "Created: ${items1[0]} ${items1[1]} ${t[0]}:${t[1]}"
            }
            else {
                noteEdited.append(
                    SimpleDateFormat(" h:mm a", Locale.getDefault())
                        .format(Date())
                )
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
//            newDateTime.addTextChangedListener {
//                viewModel.noteDateTime = it.toString()
//            }
//            time.addTextChangedListener {
//                viewModel.noteTime = it.toString()
//            }
            placeInput.addTextChangedListener {
                viewModel.noteLocation = it.toString()
            }

            saveNote.setOnClickListener {
                val viewColor = binding.fragmentNewUpdateNote.background as ColorDrawable
                val colorId = viewColor.color
                viewModel.noteColor = colorId
                viewModel.noteTodoList = todoList
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
                        viewModel.onSaveClick()
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
                    showAlert(getString(R.string.place_hint))
                }
                url?.setOnClickListener {
                    dialog.dismiss()
                    showAlert(getString(R.string.url_hint))
                }
            }

            reminderLayout.setOnClickListener {
                datePicker.show(childFragmentManager, "Date_Picker")
            }

            addColor.setOnClickListener {
                colorDialog.show()
            }
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
                    }
                    .create()
                picker.show(childFragmentManager, "color_picker")
            }

            addImage.setOnClickListener {
                val with: ImagePicker.Builder? = parentFragment?.let { it1 ->
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
                    with?.crop()
                    with?.cameraOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
//                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
                val image: LinearLayout? = dialog.findViewById(R.id.add_photo)
                image?.setOnClickListener {
                    dialog.dismiss()
                    with?.crop()
                    with?.galleryOnly()
                    with?.compress(1024)
                    with?.maxResultSize(1080, 1080)
                    with?.createIntent { Intent: Intent? ->
                        startForProfileImageResult.launch(Intent)
                    }
//                viewModel.onAddImageClick()
                }
            }
            locationLayout.setOnClickListener {
                viewModel.onLocationClick(placeInput.text)
            }
            share.setOnClickListener {

                viewModel.onShareClick(showImage)
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
//                viewModel.note?.let { it1 -> viewModel.onReminderSet(noteEntity = it1, false, binding.newDateTime.text.toString()) }
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

            //Updating UI with ViewModel Data100847802
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }
                        is AddEditTaskEvent.NavigateBackWithResult -> {
                            if(!(newDateTime.text.isNullOrEmpty())) {
                                displaySimpleNotification()
                            }
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

        }}


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
                "OK"
            ) {
                    _, _ ->
                if(editText?.text?.isNotEmpty() == true) {
                    binding.urlLink.text = editText.text.toString()
                    binding.urlParentLayout.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(context, getString(R.string.please_enter_a_link), Toast.LENGTH_LONG).show()
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
                    Toast.makeText(context, getString(R.string.please_enter_a_ace), Toast.LENGTH_LONG).show()
                    showAlert("place")
                }
            }
        }


        builder?.setNegativeButton(
            getString(R.string.cancel)
        ) { dialog, _ -> dialog.cancel() }

        builder?.show()    }

    // Reminder and Notification Logic
    private fun createNotificationChannel() {
        //Create Notification channel for SDK above 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun displaySimpleNotification() {

        val note = viewModel.getNoteData()

        notificationIntent.putExtra("note", note)

        val pendingNotificationIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingNotificationIntent
        )
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun cancelAlarm(){
        if(viewModel.requestCode != null) {
            val pendingNotificationIntent: PendingIntent? = viewModel.requestCode?.let {
                PendingIntent.getBroadcast(
                    context,
                    it,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
            alarmManager.cancel(pendingNotificationIntent)
            viewModel.requestCode = null
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getTime(): Long {

        val items1: Array<String> =
            viewModel.noteDateTime.split("-".toRegex()).toTypedArray()
        val items2: Array<String> =
            items1[3].split(":".toRegex()).toTypedArray()
        val minute = items2[1].toInt()
        val hour = items2[0].toInt()
        val day =items1[2].toInt()
        val month = items1[1].toInt() - 1
        val year = items1[0].toInt()
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }
}
