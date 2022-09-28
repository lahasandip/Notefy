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
            "How to use Notefy?", "How to create a new Note?", "What can be keep in the note?",
            "How to set Reminders?", "How to create Todo list?", "How to delete Notes?",
            "Where can i find deleted Notes?", "How to change my Profile?", "How can I change the display view?",
            "How can I sort my Notes?", "How i can share Notes?","How can I change app theme?",
            "How can I change app Language?", "How i can make Notes colorful?", "How to set enable biometric screen lock?",
        )
        val expandedText = arrayOf(
            "English", "हिन्दी", "Española", "বাংলা", "Français", "中国人", "தமிழ்",
            "Português", "bahasa Indonesia", "日本", "తెలుగు", "Русский", "मराठी", "Türk", "Italiana"
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