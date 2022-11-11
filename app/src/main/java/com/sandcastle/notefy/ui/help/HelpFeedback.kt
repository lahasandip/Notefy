package com.sandcastle.notefy.ui.help

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sandcastle.notefy.BuildConfig
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.model.Help
import com.sandcastle.notefy.databinding.FragmentHelpFeedbackBinding
import com.sandcastle.notefy.util.exhaustive

class HelpFeedback : Fragment(R.layout.fragment_help_feedback){

    private val viewModel: HelpFeedbackViewModel by viewModels()
    private var binding: FragmentHelpFeedbackBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHelpFeedbackBinding.bind(view)

        val questions = arrayOf(
            getString(R.string.q1), getString(R.string.q2), getString(R.string.q3), getString(R.string.q4), getString(R.string.q5),
            getString(R.string.q6), getString(R.string.q7), getString(R.string.q8), getString(R.string.q9), getString(R.string.q10),
            getString(R.string.q11),getString(R.string.q12), getString(R.string.q13), getString(R.string.q14), getString(R.string.q15),
        )
        val expandedText = arrayOf(
            getString(R.string.a1), getString(R.string.a2), getString(R.string.a3), getString(R.string.a4), getString(R.string.a5),
            getString(R.string.a6), getString(R.string.a7), getString(R.string.a8), getString(R.string.a9), getString(R.string.a10),
            getString(R.string.a11), getString(R.string.a12), getString(R.string.a13), getString(R.string.a14), getString(R.string.a15)
        )
        val helpList = ArrayList<Help>()

        for (i in questions.indices) {
            val helpClass = Help(
                questions[i], expandedText[i] , false
            )
            helpList.add(helpClass)
        }

        val helpAdapter = HelpAdapter(helpList)
        binding?.apply {
            helpRecyclerView.apply {
                adapter = helpAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            layoutFeedback.setOnClickListener {
                try {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=com.sandcastle.notefy")
                        )
                    )
                } catch (e: ActivityNotFoundException) {
                    startActivity(
                        Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse("https://play.google.com/store/apps/details?id=com.sandcastle.notefy")
                        )
                    )
                }
            }
            layoutWriteUs.setOnClickListener {
                val address: Array<String> = arrayOf("notefy.advise@gmail.com")
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, address)
                    putExtra(Intent.EXTRA_SUBJECT, "Please Advise")
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Android Version: ${Build.VERSION.SDK_INT},\nApp Version: ${BuildConfig.VERSION_NAME}"
                    )
                }
                if (activity?.let { it1 -> intent.resolveActivity(it1.packageManager) } != null) {
                    startActivity(intent)
                }
            }
            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}