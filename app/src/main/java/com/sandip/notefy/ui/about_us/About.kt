package com.sandip.notefy.ui.about_us

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.sandip.notefy.BuildConfig
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentAboutBinding
import com.sandip.notefy.util.exhaustive

@Suppress("IMPLICIT_CAST_TO_ANY")
class About : Fragment(R.layout.fragment_about) {

    private val viewModel: AboutViewModel by viewModels()
    private var binding: FragmentAboutBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentAboutBinding.bind(view)

        binding?.apply {
            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }
            version.text = getString(R.string.version, BuildConfig.VERSION_NAME)
            privacyPolicy.setOnClickListener{
                viewModel.onPrivacyClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is AboutViewModel.AddEditTaskEvent.NavigateToBackScreen -> {
                        findNavController().popBackStack()
                    }
                    is AboutViewModel.AddEditTaskEvent.StartPrivacyIntent -> {
                        startActivity(event.privacyIntent)
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



