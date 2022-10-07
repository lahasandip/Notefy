package com.sandip.notefy.ui.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Help
import com.sandip.notefy.databinding.FragmentHelpFeedbackBinding
import com.sandip.notefy.util.exhaustive

class HelpFeedback : Fragment(R.layout.fragment_help_feedback), HelpAdapter.OnItemClickListener {

    private val viewModel: HelpFeedbackViewModel by viewModels()
    private lateinit var binding: FragmentHelpFeedbackBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHelpFeedbackBinding.bind(view)

        val questions = arrayOf(
            "How to use Notefy?", "How to create a new Note?", "What can be added in the note?",
            "How to set Reminders?", "How to create Todo list?", "How to delete Notes?",
            "Where can I find deleted Notes?", "How to update my Profile?", "How can I change the display view?",
            "How can I sort my Notes?", "How can I share Notes?","How can I change app theme?",
            "How can I change app Language?", "How can I make Notes colorful?", "How to secure my app ?",
        )
        val expandedText = arrayOf(
            "Notefy is used to take Notes, add Reminders, create Todo list and much more. You can save your notes with images, colors, " +
            "place and URL. You can filter notes based on your preferences, search and share your notes. It supports Dark theme and light " +
            "theme. For security it has screen lock feature. You can choose your preferred language, currently Notefy supports 15 different" +
            " languages. For any help you can either read FAQ's or contact us.",

            "You can create a new note in two ways:\n1. Click on the + button at bottom-right side of Home screen. Or\n" +
            "2. Click on Menu icon of top action bar, now click on New Note tab.\nA new window will open to create a new note.",

            "You can add the following features in the Note:\n1. Note Title\n2. Note Description\n3. Reminder\n4. Place\n5. URL\n" +
            "6. Colored background\n7. Image\n8. Todo list\n9. You can also bookmark the note",

            "Click on the Add box icon at bottom side of New note screen, select Reminder. A calender will popup, choose the date " +
            "you want to set the reminder, click on OK. Now a clock will popup, choose the time and click on OK. Now you have " +
            "successfully set the reminder. A confirmation message will also display. Now based on the date and time you've chosen " +
            "a Notification with note title, description and Image will be notified.",

            "Click on the Checklist icon at bottom side of New note screen. Todo Checklist popup will occur. You can add, delete, update " +
            "any number of todo items with tick boxes, once completed click on the Done button.",

            "You can delete notes in the following ways:\n1. To delete a single note, you can simply swipe it to left or right side in the" +
            " home screen.\n2. To delete multiple notes at the same time, long press on any note and select the notes you want to delete or" +
            " click on select all in the action bar, then click on Delete icon.\n3. Open the note you want to delete, then click on delete " +
            "icon at the top action bar.\nNote: You can UNDO the notes right after you delete them.",

            "Once you delete the notes, it will be added in Recycle bin. And from there either you can restore them or delete them forever." +
            "You can find Recycle bin from Navigation drawer",

            "You can update your profile photo, name, email and phone in the profile page. You can open your profile by \n1. Click on profile " +
            "photo on the home screen at top-right corner. Or\n2. Click on Profile tab from navigation drawer.",

            "To change the view, you can switch between grid view and linear view by clicking on the grid button right side of the Search view." +
            "in the Home screen",

            "Once you add notes, you can display them by any of the following order:\n1. Bookmarked\n2. Title by Asc order\n3. Title by Desc order\n" +
            "4. Newest to oldest\n5. Oldest to newest.\n To do so click on the Sort by icon on the top action bar of the home screen. " +
            "The default order is Newest to oldest",

            "To share your notes with friends, you can open the note that you want to share, click on the Share button on top-right action bar. You can" +
            " choose any medium to share it.",

            "Open the navigation drawer from left side, toggle the Dark theme switch to enable Dark mode, and disable the switch for light mode.",

            "Currently Notefy supports 15 different languages. English is the default language, you can change any language of your choice. Open the " +
            "language screen from left side navigation drawer and select any language. Click on continue.",

            "You can change notes background color of your choice. Click on color icon in new note screen. You can choose either from default colors " +
            "or from the color picker.",

            "To secure your app, you can enable screen lock feature from left side navigation drawer, it will accept your phone password or biometric " +
            "or pattern. You can enable or disable based on your need."
        )
        val helpList = ArrayList<Help>()

        for (i in questions.indices) {
            val helpClass = Help(
                questions[i], expandedText[i] , false
            )
            helpList.add(helpClass)
        }

        val helpAdapter = HelpAdapter(helpList, this)
        binding.apply {
            helpRecyclerView.apply {
                adapter = helpAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            layoutFeedback.setOnClickListener {
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.imangi.templerun")))
                } catch (e: ActivityNotFoundException) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.imangi.templerun")))
                }
            }
            layoutWriteUs.setOnClickListener {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:") // only email apps should handle this
                    putExtra(Intent.EXTRA_EMAIL, "sandiplaha206@gmail.com")
                    putExtra(Intent.EXTRA_SUBJECT, "subject")
                    putExtra(Intent.EXTRA_TEXT, "Body")

                }
                if (activity?.let { it1 -> intent.resolveActivity(it1.packageManager) } != null) {
                    startActivity(intent)
                }
            }
            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect { event ->
                    when (event) {
                        is HelpFeedbackViewModel.AddEditTaskEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }

        }}


    override fun onItemClick(flag: Int) {

    }
}