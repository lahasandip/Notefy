package com.sandip.notefy.ui.newupdate

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.*
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
import androidx.lifecycle.asLiveData
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
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import vadiole.colorpicker.ColorModel
import vadiole.colorpicker.ColorPickerDialog
import java.text.SimpleDateFormat
import java.util.*


const val CHANNEL_ID: String = "4"
const val CHANNEL_NAME: String = "Notefy"
const val CHANNEL_DESCRIPTION = "Reminder Message"
@AndroidEntryPoint
class NewUpdateNote : Fragment(R.layout.fragment_new_update_note) {

    private val viewModel: NewUpdateNoteViewModel by viewModels()
    private lateinit var binding: FragmentNewUpdateNoteBinding
    private lateinit var date: String
    private lateinit var datePicker: MaterialDatePicker<Long>
    private lateinit var timePicker: MaterialTimePicker
    private var todoList = ArrayList<Todo>()

    //Companion Object for Temp List
    companion object {
        var recylerView: RecyclerView? = null
        var todoAdapter: NewUpdateAdapter? = null
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
                    //Image Uri will not be null for RESULT_OK
                    val fileUri = data?.data!!
                    binding.imageLayout.visibility = View.VISIBLE
                    context?.let { Glide.with(it).load(fileUri).into(binding.showImage) }
                    viewModel.noteImage = fileUri.toString()
                } else if (resultCode == ImagePicker.RESULT_ERROR) {
                    Toast.makeText(context, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Task Cancelled", Toast.LENGTH_SHORT).show()
                }
            }

        val colorDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        colorDialog.setContentView(R.layout.add_color_dialog)
        colorDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        colorDialog.window?.setGravity(Gravity.BOTTOM)


        val frame_white: FrameLayout? = colorDialog.findViewById(R.id.frame_no_gradient)
        val white: ImageView? = colorDialog.findViewById(R.id.no_gradient)
        white?.setImageResource(R.drawable.ic_baseline_done_24)

        val frame_lightsteelblue: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_1)
        val lightsteelblue: ImageView? = colorDialog.findViewById(R.id.gradient_1)

        val frame_aquamarine: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_2)
        val aquamarine: ImageView? = colorDialog.findViewById(R.id.gradient_2)

        val frame_grey: FrameLayout? = colorDialog.findViewById(R.id.frame_gradient_3)
        val grey: ImageView? = colorDialog.findViewById(R.id.gradient_3)

        val frame_darkgrey: FrameLayout? = colorDialog.findViewById(R.id.frame_gradient_4)
        val darkgrey: ImageView? = colorDialog.findViewById(R.id.gradient_4)

        val frame_lightcyan: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_5)
        val lightcyan: ImageView? = colorDialog.findViewById(R.id.gradient_5)

        val frame_lightgoldenyellow: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_6)
        val lightgoldenyellow: ImageView? =
            colorDialog.findViewById(R.id.gradient_6)

        val frame_lightgreen: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_7)
        val lightgreen: ImageView? = colorDialog.findViewById(R.id.gradient_7)

        val frame_palegoldenrod: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_8)
        val palegoldenrod: ImageView? = colorDialog.findViewById(R.id.gradient_8)

        val frame_palevioletred: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_9)
        val palevioletred: ImageView? = colorDialog.findViewById(R.id.gradient_9)

        val frame_powderblue: FrameLayout? =
            colorDialog.findViewById(R.id.frame_gradient_10)
        val powderblue: ImageView? = colorDialog.findViewById(R.id.gradient_10)

        val colorPicker: Button? = colorDialog.findViewById(R.id.color_picker)









        //Todo list view popup
        val todoDialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        todoDialog.setContentView(R.layout.todo_listview)
        todoDialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)

        todoDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        todoDialog.window?.setGravity(Gravity.BOTTOM)
        val addToList = todoDialog.findViewById<ImageView>(R.id.addTodo)
        val checkBoxTodo =   todoDialog.findViewById<CheckBox>(R.id.todoCheck)
        val descriptionTodo =   todoDialog.findViewById<EditText>(R.id.todoDesc)
        recylerView =   todoDialog.findViewById(R.id.todo_listview)
        val btn =   todoDialog.findViewById<Button>(R.id.done)

        //Date & Time Picker
        val calendar = Calendar.getInstance()
        var hour = calendar.get(Calendar.HOUR_OF_DAY)
        var minute = calendar.get(Calendar.MINUTE)
        datePicker =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select date")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()


        val isSystem24Hour = DateFormat.is24HourFormat(context)
        val clockFormat = if (isSystem24Hour) TimeFormat.CLOCK_24H else TimeFormat.CLOCK_12H

        timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(clockFormat)
                .setHour(hour)
                .setMinute(minute)
                .setTitleText("Select time")
                .build()
        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it
            val format = SimpleDateFormat("yyyy-MM-dd")
            date = format.format(utc.time)
            val format2=SimpleDateFormat("EEE, MMM d, ''yy")
            val date2 = format2.format(utc.time)

            (date2.toString()).also { binding.date.text = it }
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

            if(!(viewModel.noteTitle.isNullOrEmpty())) {
                "${timePicker.hour}:${timePicker.minute}".also { binding.time.text = it }
                binding.reminderParentLayout.visibility = View.VISIBLE
                displaySimpleNotification()
                Snackbar.make(requireView(), "Reminder set Successfully ", Snackbar.LENGTH_LONG).show()

            }
            else{
                Snackbar.make(requireView(), "Please Add a Title to Set Reminder", Snackbar.LENGTH_LONG).show()

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
            if (!(viewModel.noteUrl.isNullOrEmpty())) {
                urlLink.text = viewModel.noteUrl
                urlParentLayout.visibility = View.VISIBLE
            }
            if ((!(viewModel.noteDate.isNullOrEmpty())) && (!(viewModel.noteTime.isNullOrEmpty()))) {
                date.text = viewModel.noteDate
                time.text = viewModel.noteTime
                reminderParentLayout.visibility = View.VISIBLE
            }
            if (!(viewModel.noteLocation.isNullOrEmpty())) {
                placeInput.text = viewModel.noteLocation
                locationParentLayout.visibility = View.VISIBLE
            }
            if (viewModel.noteColor != null) {
                fragmentNewUpdateNote.setBackgroundColor(viewModel.noteColor)
            }
            when(viewModel.noteColor){
                -13359 ->  { lightsteelblue?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -2252579 -> {aquamarine?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -16718218 -> {grey?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -43230 -> { darkgrey?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -6982195 -> { lightcyan?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -12490271 -> { lightgoldenyellow?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -23296 -> {lightgreen?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -38476 -> { palegoldenrod?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -7650029 -> { palevioletred?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                -9404272 -> { powderblue?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)}
                0 -> white?.setImageResource(R.drawable.ic_baseline_done_24)
                else -> white?.setImageResource(0)
            }

            Log.d("color", viewModel.noteColor.toString())

            if (viewModel.noteImage != null) {
                val imageUri = Uri.parse(viewModel.noteImage)
                context?.let { Glide.with(it).load(imageUri).into(showImage) }
                imageLayout.visibility = View.VISIBLE
            }

            if (viewModel.noteTodoList != null) {
                todoList = viewModel.noteTodoList as ArrayList<Todo>
                todoAdapter = context?.let {
                    NewUpdateAdapter(
                        it,
                        todoList,
                    )
                }
                recylerView?.setHasFixedSize(true)
                recylerView?.layoutManager = LinearLayoutManager(context)
                recylerView?.adapter = todoAdapter
                taskLayout.visibility = View.VISIBLE
                btn?.visibility=View.VISIBLE
            }

            if(viewModel.note?.createdDateFormatted != null){
                val da = viewModel.note?.createdDateFormatted
                val items1: Array<String> =
                    da?.split(" ".toRegex())?.toTypedArray() ?: arrayOf("")
                noteEdited.text = "Edited: ${items1[0]} ${items1[1]} ${items1[3]}"
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

            important.setOnCheckedChangeListener() { _, isChecked ->
                viewModel.noteImportance = isChecked
            }
            urlLink.addTextChangedListener {
                viewModel.noteUrl = it.toString()
            }
            date.addTextChangedListener {
                viewModel.noteDate = it.toString()
            }
            time.addTextChangedListener {
                viewModel.noteTime = it.toString()
            }
            placeInput.addTextChangedListener {
                viewModel.noteLocation = it.toString()
            }

            saveNote.setOnClickListener {
                val viewColor = binding.fragmentNewUpdateNote.background as ColorDrawable
                val colorId = viewColor.color
                viewModel.noteColor = colorId
                viewModel.noteTodoList = todoList

                viewModel.onSaveClick()

            }

            deleteNote.setOnClickListener {

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm deletion")
                    .setMessage("Do you want to delete the note ?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes") { _, _ ->
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
//                viewModel.onAddFeaturesClick()
                val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
                dialog.setContentView(R.layout.add_features_dialog)
                dialog.show()
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
                val reminder: LinearLayout? = dialog.findViewById(R.id.add_reminder_layout)
                val place: LinearLayout? = dialog.findViewById(R.id.add_place_layout)
                val url: LinearLayout? = dialog.findViewById(R.id.add_url_layout)


                reminder?.setOnClickListener {
//                    viewModel.onReminderClick()
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
            addColor.setOnClickListener {
                colorDialog.show()
            }
                frame_white?.setOnClickListener {
                    white?.setImageResource(R.drawable.ic_baseline_done_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(0)
//                    viewModel.onColorTick(0)
                }

                frame_lightsteelblue?.setOnClickListener {
                    lightsteelblue?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.PaleVioletRed, null))
//                    viewModel.onColorTick(1)

                }

                frame_aquamarine?.setOnClickListener {
                    aquamarine?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Plum, null))
//                    viewModel.onColorTick(2)

                }

                frame_grey?.setOnClickListener {
                    grey?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.LimeGreen, null))
//                    viewModel.onColorTick(3)

                }

                frame_darkgrey?.setOnClickListener {
                    darkgrey?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.red, null))
//                    viewModel.onColorTick(4)

                }

                frame_lightcyan?.setOnClickListener {
                    lightcyan?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Bisque, null))
//                    viewModel.onColorTick(5)
                }

                frame_lightgoldenyellow?.setOnClickListener {
                    lightgoldenyellow?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.RoyalBlue, null))
//                    viewModel.onColorTick(6)

                }

                frame_lightgreen?.setOnClickListener {
                    lightgreen?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.Orange, null))
//                    viewModel.onColorTick(7)

                }

                frame_palegoldenrod?.setOnClickListener {
                    palegoldenrod?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.HotPink, null))

//                    viewModel.onColorTick(8)
                }

                frame_palevioletred?.setOnClickListener {
                    palevioletred?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    powderblue?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.SaddleBrown, null))
//                    viewModel.onColorTick(9)

                }

                frame_powderblue?.setOnClickListener {
                    powderblue?.setImageResource(R.drawable.ic_baseline_done_24)
                    white?.setImageResource(R.drawable.ic_outline_format_color_reset_24)
                    lightsteelblue?.setImageResource(0)
                    aquamarine?.setImageResource(0)
                    grey?.setImageResource(0)
                    darkgrey?.setImageResource(0)
                    lightcyan?.setImageResource(0)
                    lightgoldenyellow?.setImageResource(0)
                    lightgreen?.setImageResource(0)
                    palegoldenrod?.setImageResource(0)
                    palevioletred?.setImageResource(0)
                    binding.fragmentNewUpdateNote.setBackgroundColor(resources.getColor(R.color.SlateGray, null))
//                    viewModel.onColorTick(10)

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
                        null
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
                        null
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
                    todoList.add(Todo(checkBoxTodo?.isChecked, descriptionTodo?.text.toString()))
                    todoAdapter = NewUpdateAdapter(requireContext(), todoList)
                    recylerView?.setHasFixedSize(true)
                    recylerView?.layoutManager = LinearLayoutManager(context)
                    recylerView?.adapter = todoAdapter
                    todoAdapter?.notifyDataSetChanged()

                }


                descriptionTodo?.setText("")
                checkBoxTodo?.isChecked = false
                println("My todo list $todoList")
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
                date.text = null
                time.text = null
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

//            }

            //Updating UI with ViewModel Data
            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is NewUpdateNoteViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                            Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_LONG).show()
                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                            binding.noteTitle.clearFocus()
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()
                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.NavigateToBackAfterDelete -> {
                            setFragmentResult(
                                "add_edit_delete_request",
                                bundleOf("add_edit_delete_result" to event.result)
                            )
                            findNavController().popBackStack()

                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.ShareIntent -> {
                            startActivity(event.shareIntent)
                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.StartLocationIntent -> {
                            startActivity(event.mapIntent)
                        }
                        is NewUpdateNoteViewModel.AddEditTaskEvent.NavigateToTodoScreen -> {
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
            builder?.setTitle("URL")
            editText?.hint = "https://www.google.com"
            builder?.setPositiveButton(
                "OK"
            ) {
                    _, _ ->
                if(editText?.text?.isNotEmpty() == true) {
                    binding.urlLink.text = editText?.text.toString()
                    binding.urlParentLayout.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(context, "Please enter a link", Toast.LENGTH_LONG).show()
                    showAlert("url")
                }
            }


        }
        else if(s=="place"){
            builder?.setTitle("Place")
            editText?.hint = "New York, United States"
            builder?.setPositiveButton(
                "OK"
            ) {
                    _, _ ->
                if(editText?.text?.isNotEmpty() == true) {
                    binding.placeInput.text = editText?.text.toString()
                    binding.locationParentLayout.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(context, "Please enter a place", Toast.LENGTH_LONG).show()
                    showAlert("place")
                }
            }
        }


        builder?.setNegativeButton(
            "Cancel"
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
            println("sandip chanel created")
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun displaySimpleNotification() {
        println("sandip enter display function")

        val notificationIntent = Intent(context, Notifications::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            println("sandip notification intent created")
        }

        notificationIntent.putExtra("titleExtra", viewModel.noteTitle)
        notificationIntent.putExtra("messageExtra", viewModel.noteDescription)
        notificationIntent.putExtra("imageExtra", viewModel.noteImage)

        val pendingNotificationIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        println("sandip pending notification intent created")



//        val titleStatusChangeListener= binding.datePicker.year

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingNotificationIntent
        )
//        showAlert(
//            time,
//            title,
//            message
//        )
//        fun cancelAlarm(){
//        alarmManager.cancel(pendingNotificationIntent)
//    }
        println("sandip alarm manager created $time")

    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun getTime(): Long {
        val minute = timePicker.minute
        val hour = timePicker.hour
        val items1: Array<String> =
            date.split("-".toRegex()).toTypedArray()

        val day =items1[2].toInt()
        val month = items1[1].toInt() - 1
        val year = items1[0].toInt()
        println("sandip $day $month $year $minute $hour")

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }
}
