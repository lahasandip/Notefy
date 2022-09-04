package com.sandip.notefy.ui.newupdate

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.text.format.DateFormat
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentNewUpdateNoteBinding
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

const val CHANNEL_ID: String = "1"
const val CHANNEL_NAME: String = "Developer"
const val CHANNEL_DESCRIPTION = "Notification Message"
@AndroidEntryPoint
class NewUpdateNote : Fragment(R.layout.fragment_new_update_note) {
    private val REQUEST_IMAGE_CAPTURE = 1
    private val SELECT_PICTURE = 2
    private val viewModel: NewUpdateNoteViewModel by viewModels()
    private lateinit var binding : FragmentNewUpdateNoteBinding
    private lateinit var date:String
    private lateinit var datePicker:MaterialDatePicker<Long>
    private lateinit var timePicker:MaterialTimePicker



    companion object {
        val completed = ArrayList<Boolean>()
        val todoDescription = ArrayList<String>()
        var lv: ListView? = null
        var adp: ListViewBAdapter? = null

    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentNewUpdateNoteBinding.bind(view)
        createNotificationChannel()

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
                .setHour(12)
                .setMinute(10)
                .setTitleText("Select time")
                .build()
        datePicker.addOnPositiveButtonClickListener {
            // Respond to positive button click.
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = it
            val format = SimpleDateFormat("yyyy-MM-dd")
            date = format.format(utc.time)

            (datePicker.headerText).also { binding.date.text = it }
//             displaySimpleNotification(timePicker,datePicker)
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
            "${timePicker.hour}:${timePicker.minute}".also { binding.time.text = it }
            binding.reminderLayout.visibility = View.VISIBLE

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

//        val viewColor = binding.newUpdateNote.background as ColorDrawable
//        val colorId = viewColor.color
//        viewModel.noteColor = colorId
//
//        val bitmap: Bitmap? = (binding.showImage.drawable as? BitmapDrawable)?.bitmap
//        val height = bitmap?.height
//        val width = bitmap?.width
//        val bit= bitmap?.let { Bitmap.createScaledBitmap(it, width!!,height!!,false) }
//        viewModel.noteImage = bit

        binding.apply {
            noteTitle.setText(viewModel.noteTitle)
            noteDescription.setText(viewModel.noteDescription)
            important.isChecked = viewModel.noteImportance
            if (!(viewModel.noteUrl.isNullOrEmpty())) {
                urlLink.text = viewModel.noteUrl
                urlLinkLayout.visibility = View.VISIBLE
            }
            if ((!(viewModel.noteDate.isNullOrEmpty())) && (!(viewModel.noteTime.isNullOrEmpty()))) {
                date.text = viewModel.noteDate
                time.text = viewModel.noteTime
                reminderLayout.visibility = View.VISIBLE
            }
            if (!(viewModel.noteLocation.isNullOrEmpty())) {
                placeInput.text = viewModel.noteLocation
                locationLayout.visibility = View.VISIBLE
            }
            newUpdateNote.setBackgroundColor(viewModel.noteColor)
            if (viewModel.noteImage != null) {
                showImage.setImageBitmap(viewModel.noteImage)
                showImage.visibility = View.VISIBLE
            }
            noteEdited.text = "Edited: ${
                viewModel.note?.createdDateFormatted ?: LocalTime.now()
                    .format(DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT))
            }"


//            checkBoxImportant.jumpDrawablesToCurrentState()
//            textViewDateCreated.isVisible = viewModel.task != null

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
                val viewColor = binding.newUpdateNote.background as ColorDrawable
                val colorId = viewColor.color
                viewModel.noteColor = colorId
                displaySimpleNotification()
                viewModel.onSaveClick(completed, todoDescription)
            }
            deleteNote.setOnClickListener {

                AlertDialog.Builder(requireContext())
                    .setTitle("Confirm deletion")
                    .setMessage("Do you want to delete the note permanently?")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Yes") { _, _ ->
                        viewModel.onConfirmDeleteClick()
                    }
                    .create()
                    .show()
            }
            back.setOnClickListener {
                viewModel.onBackClick()
            }
            addFeatures.setOnClickListener {
//                viewModel.onAddFeaturesClick()
                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setContentView(R.layout.add_features_dialog)
                dialog.show()
                dialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                dialog.window?.setGravity(Gravity.BOTTOM)
                val reminder: LinearLayout = dialog.findViewById(R.id.add_reminder_layout)
                val place: LinearLayout = dialog.findViewById(R.id.add_place_layout)
                val url: LinearLayout = dialog.findViewById(R.id.add_url_layout)
                reminder.setOnClickListener {
//                    viewModel.onReminderClick()
                    dialog.dismiss()
                    datePicker.show(childFragmentManager, "Date_Picker")
                }
                place.setOnClickListener {
                    dialog.dismiss()
                    showAlert("place")
                }
                url.setOnClickListener {
                    dialog.dismiss()
                    showAlert("url")
                }


            }
            addColor.setOnClickListener {
                val colorDialog = Dialog(requireContext())
                colorDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                colorDialog.setContentView(R.layout.add_color_dialog)
                colorDialog.show()
                colorDialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                colorDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                colorDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                colorDialog.window?.setGravity(Gravity.BOTTOM)


                val frame_white: FrameLayout = colorDialog.findViewById(R.id.frame_white)
                val white: ImageView = colorDialog.findViewById(R.id.white)
                white.setImageResource(R.drawable.ic_baseline_done_24)

                val frame_lightsteelblue: FrameLayout =
                    colorDialog.findViewById(R.id.frame_lightsteelblue)
                val lightsteelblue: ImageView = colorDialog.findViewById(R.id.lightsteelblue)

                val frame_aquamarine: FrameLayout =
                    colorDialog.findViewById(R.id.frame_aquamarine)
                val aquamarine: ImageView = colorDialog.findViewById(R.id.aquamarine)

                val frame_grey: FrameLayout = colorDialog.findViewById(R.id.frame_grey)
                val grey: ImageView = colorDialog.findViewById(R.id.grey)

                val frame_darkgrey: FrameLayout = colorDialog.findViewById(R.id.frame_darkgrey)
                val darkgrey: ImageView = colorDialog.findViewById(R.id.darkgrey)

                val frame_lightcyan: FrameLayout =
                    colorDialog.findViewById(R.id.frame_lightcyan)
                val lightcyan: ImageView = colorDialog.findViewById(R.id.lightcyan)

                val frame_lightgoldenyellow: FrameLayout =
                    colorDialog.findViewById(R.id.frame_lightgoldenyellow)
                val lightgoldenyellow: ImageView =
                    colorDialog.findViewById(R.id.lightgoldenyellow)

                val frame_lightgreen: FrameLayout =
                    colorDialog.findViewById(R.id.frame_lightgreen)
                val lightgreen: ImageView = colorDialog.findViewById(R.id.lightgreen)

                val frame_palegoldenrod: FrameLayout =
                    colorDialog.findViewById(R.id.frame_palegoldenrod)
                val palegoldenrod: ImageView = colorDialog.findViewById(R.id.palegoldenrod)

                val frame_palevioletred: FrameLayout =
                    colorDialog.findViewById(R.id.frame_palevioletred)
                val palevioletred: ImageView = colorDialog.findViewById(R.id.palevioletred)

                val frame_powderblue: FrameLayout =
                    colorDialog.findViewById(R.id.frame_powderblue)
                val powderblue: ImageView = colorDialog.findViewById(R.id.powderblue)

                val frame_rosybrown: FrameLayout =
                    colorDialog.findViewById(R.id.frame_rosybrown)
                val rosybrown: ImageView = colorDialog.findViewById(R.id.rosybrown)

                val frame_sandybrown: FrameLayout =
                    colorDialog.findViewById(R.id.frame_sandybrown)
                val sandybrown: ImageView = colorDialog.findViewById(R.id.sandybrown)

                val frame_thistle: FrameLayout = colorDialog.findViewById(R.id.frame_thistle)
                val thistle: ImageView = colorDialog.findViewById(R.id.thistle)

                val frame_violet: FrameLayout = colorDialog.findViewById(R.id.frame_violet)
                val violet: ImageView = colorDialog.findViewById(R.id.violet)

                frame_white.setOnClickListener {
                    white.setImageResource(R.drawable.ic_baseline_done_24)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#FFFFFFFF"))
                }

                frame_lightsteelblue.setOnClickListener {
                    lightsteelblue.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#B0C4DE"))
                }

                frame_aquamarine.setOnClickListener {
                    aquamarine.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#7FFFD4"))
                }

                frame_grey.setOnClickListener {
                    grey.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#E8E9EB"))
                }

                frame_darkgrey.setOnClickListener {
                    darkgrey.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#A9A9A9"))
                }

                frame_lightcyan.setOnClickListener {
                    lightcyan.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#E0FFFF"))
                }

                frame_lightgoldenyellow.setOnClickListener {
                    lightgoldenyellow.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#FAFAD2"))
                }

                frame_lightgreen.setOnClickListener {
                    lightgreen.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#CEFAD0"))
                }

                frame_palegoldenrod.setOnClickListener {
                    palegoldenrod.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#EEE8AA"))
                }

                frame_palevioletred.setOnClickListener {
                    palevioletred.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#FFCBD1"))
                }

                frame_powderblue.setOnClickListener {
                    powderblue.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#B0E0E6"))
                }

                frame_rosybrown.setOnClickListener {
                    rosybrown.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#BC8F8F"))
                }

                frame_sandybrown.setOnClickListener {
                    sandybrown.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#F4A460"))
                }

                frame_thistle.setOnClickListener {
                    thistle.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    violet.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#D8BFD8"))
                }

                frame_violet.setOnClickListener {
                    violet.setImageResource(R.drawable.ic_baseline_done_24)
                    white.setImageResource(0)
                    lightsteelblue.setImageResource(0)
                    aquamarine.setImageResource(0)
                    grey.setImageResource(0)
                    darkgrey.setImageResource(0)
                    lightcyan.setImageResource(0)
                    lightgoldenyellow.setImageResource(0)
                    lightgreen.setImageResource(0)
                    palegoldenrod.setImageResource(0)
                    palevioletred.setImageResource(0)
                    powderblue.setImageResource(0)
                    rosybrown.setImageResource(0)
                    sandybrown.setImageResource(0)
                    thistle.setImageResource(0)
                    binding.newUpdateNote.setBackgroundColor(Color.parseColor("#EFC9FE"))
                }

            }

            addImage.setOnClickListener {
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
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                }
                val image: LinearLayout = dialog.findViewById(R.id.add_photo)
                image.setOnClickListener {
                    dialog.dismiss()
                    val i = Intent()
                    i.type = "image/*"
                    i.action = Intent.ACTION_GET_CONTENT
                    startActivityForResult(
                        Intent.createChooser(i, "Select Picture"),
                        SELECT_PICTURE
                    )
                }
//                viewModel.onAddImageClick()
            }
            locationLayout.setOnClickListener {
                viewModel.onLocationClick(placeInput.text)
            }
            share.setOnClickListener {
                viewModel.onShareClick()
            }

            addTask.setOnClickListener {
//                todoLayout.visibility = View.VISIBLE
                val todoDialog = Dialog(requireContext())
                todoDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                todoDialog.setContentView(R.layout.todo_listview)
                todoDialog.show()
                todoDialog.window?.setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                )
                todoDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                todoDialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                todoDialog.window?.setGravity(Gravity.CENTER)

                val ff = todoDialog.findViewById<ImageView>(R.id.addTodo)
                val aa =   todoDialog.findViewById<CheckBox>(R.id.todoCheck)
                val bb =   todoDialog.findViewById<EditText>(R.id.todoDesc)
                lv =   todoDialog.findViewById<ListView>(R.id.todo_listview)
                val btn =   todoDialog.findViewById<Button>(R.id.done)


                ff.setOnClickListener {
                    completed.add(aa.isChecked)
                    todoDescription.add(bb.text.toString())
                    println(completed + " \n" + todoDescription)
                    adp = ListViewBAdapter(requireContext(), completed, todoDescription)
                    binding.todo.apply {
                        lv?.adapter = adp
                        adp?.notifyDataSetChanged()

                    }
                    bb.setText("")
                    aa.isChecked = false
                    btn.visibility=View.VISIBLE
                }
                btn.setOnClickListener {
//                    if(!(bb.text.isNullOrEmpty())){
//                        binding.td.visibility = View.VISIBLE
//                    }
//                    else{
//                        binding.td.visibility = View.GONE
//                    }

                    todoDialog.dismiss()
                }

            }


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
                        is NewUpdateNoteViewModel.AddEditTaskEvent.DisplayDialog -> {
                            event.dialog.show(parentFragmentManager, "Add Feature Popup")
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.showImage.visibility = View.VISIBLE
        if ((requestCode == REQUEST_IMAGE_CAPTURE) && (resultCode == AppCompatActivity.RESULT_OK)) {

            val imageBitmap = data?.extras?.get("data") as Bitmap
//            Bitmap.createScaledBitmap(imageBitmap, 120,120,false)
            Glide.with(this).load(imageBitmap).into(binding.showImage);

//            binding.setImage.setImageBitmap(imageBitmap)
        } else if (requestCode == SELECT_PICTURE && resultCode == AppCompatActivity.RESULT_OK) {
            val selectedImageUri: Uri? = data?.data
            if (null != selectedImageUri) {
                Glide.with(this).load(selectedImageUri).into(binding.showImage);

//                binding.setImage.setImageURI(selectedImageUri)
            }
        } else {
            binding.showImage.visibility = View.GONE
        }
    }

    private fun showAlert(s: String) {
        val builder = context?.let { AlertDialog.Builder(it) }
        val input = EditText(context)
        input.inputType = InputType.TYPE_TEXT_VARIATION_URI
        input.background = null
        builder?.setView(input)

        if(s=="url"){
            builder?.setTitle("URL")
            input.hint = "https://"
            builder?.setPositiveButton(
                "OK"
            ) {
                    _, _ ->
                if(input.text.isNotEmpty()) {
                    binding.urlLink.text = input.text.toString()
                    binding.urlLinkLayout.visibility = View.VISIBLE
                }
                else{
                    Toast.makeText(context, "Please enter a link", Toast.LENGTH_LONG).show()
                    showAlert("url")
                }
            }


        }
        else if(s=="place"){
            builder?.setTitle("Place")
            input.hint = ""
            builder?.setPositiveButton(
                "OK"
            ) {
                    _, _ ->
                if(input.text.isNotEmpty()) {
                    binding.placeInput.text = input.text.toString()
                    binding.locationLayout.visibility = View.VISIBLE
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

    override fun onDestroyView() {
        super.onDestroyView()
//            colorImage()

//        val viewColor = binding.newUpdateNote.background as ColorDrawable
//        val colorId = viewColor.color
//        viewModel.noteColor = colorId
//
//        val bitmap: Bitmap? = (binding.showImage.drawable as? BitmapDrawable)?.bitmap
//        val height = bitmap?.height
//        val width = bitmap?.width
//        val bit= bitmap?.let { Bitmap.createScaledBitmap(it, width!!,height!!,false) }
//        viewModel.noteImage = bit
    }
//    fun colorImage() {
//        val viewColor = binding.newUpdateNote.background as ColorDrawable
//        val colorId = viewColor.color
//        viewModel.noteColor = colorId
//
//        val bitmap: Bitmap? = (binding.showImage.drawable as? BitmapDrawable)?.bitmap
//        val height = bitmap?.height
//        val width = bitmap?.width
//        val bit= bitmap?.let { Bitmap.createScaledBitmap(it, width!!,height!!,false) }
//        viewModel.noteImage = bit
//    }

    fun removeItem(position: Int) {
        completed?.removeAt(position)
        todoDescription?.removeAt(position)
        lv?.setAdapter(adp)
        adp?.notifyDataSetChanged()

    }

    fun update(position: Int, checked: Boolean) {
        completed?.set(position,checked)
        adp?.notifyDataSetChanged()
    }

    private fun createNotificationChannel() {
        //Create Notification channel for SDK above 25
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            channel.description = CHANNEL_DESCRIPTION
            val notificationManager = context?.getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            println("sandip chanel created")
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    private fun displaySimpleNotification() {
        println("sandip enter display function")

        val notificationIntent = Intent(context!!, Notification::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            println("sandip notification intent created")

        }
        val pendingNotificationIntent: PendingIntent = PendingIntent.getBroadcast(
            context,
            System.currentTimeMillis().toInt(),
            notificationIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        println("sandip pending notification intent created")


        val title = "Sandip"
        val message = "Lahan"
        notificationIntent.putExtra(titleExtra, title)
        notificationIntent.putExtra(messageExtra, message)
//        val titleStatusChangeListener= binding.datePicker.year

        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val time = getTime()
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            time,
            pendingNotificationIntent
        )
        showAlert(
            time,
            title,
            message
        )
        println("sandip alarm manager created")

    }

    private fun showAlert(time: Long, title: String, message: String) {
        val date = Date(time)
        val dateFormat = android.text.format.DateFormat.getLongDateFormat(context)
        val timeFormat = android.text.format.DateFormat.getTimeFormat(context)

        android.app.AlertDialog.Builder(context)
            .setTitle("")
            .setMessage(
                "Title: $title\nMessage $message\nAt " + dateFormat.format(
                    date
                ) + " " + timeFormat.format(date)
            )
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getTime(): Long {
        val minute = timePicker.minute
        val hour = timePicker.hour
        val items1: Array<String> =
            date.split("-".toRegex()).toTypedArray()

        val day =items1[2].toInt()
        val month = items1[1].toInt()
        val year = items1[0].toInt()
        println("sandip $day $month $year")
        val calendar = Calendar.getInstance()
        calendar.set(year, month, day, hour, minute)
        return calendar.timeInMillis
    }
}