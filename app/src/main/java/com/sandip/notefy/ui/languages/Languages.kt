package com.sandip.notefy.ui.languages

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentLanguagesBinding
import com.sandip.notefy.ui.home.HomeDirections
import com.sandip.notefy.ui.home.HomeViewModel
import com.sandip.notefy.util.exhaustive

class Languages : Fragment(R.layout.fragment_languages) {

    private val viewModel : LanguagesViewModel by viewModels()
    private lateinit var binding : FragmentLanguagesBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)


        binding.apply {
            ok.setOnClickListener {
                viewModel.onOkClick()
            }
//            image1.setOnClickListener {
//                image2.isChecked = false
//                image3.isChecked = false
//                image4.isChecked = false
//                image5.isChecked = false
//            }
//            image2.setOnClickListener {
//                image1.isChecked = false
//                image3.isChecked = false
//                image4.isChecked = false
//                image5.isChecked = false
//
//            }
//            image3.setOnClickListener {
//                image1.isChecked = false
//                image2.isChecked = false
//                image4.isChecked = false
//                image5.isChecked = false
//
//            }
//            image4.setOnClickListener {
//                image1.isChecked = false
//                image2.isChecked = false
//                image3.isChecked = false
//                image5.isChecked = false
//
//            }
//            image5.setOnClickListener {
//                image1.isChecked = false
//                image2.isChecked = false
//                image3.isChecked = false
//                image4.isChecked = false
//
//            }

        }




        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {

                    is LanguagesViewModel.TasksEvent.NavigateToHomeScreen -> {
                        val action =
                            LanguagesDirections.actionLanguagesToHome(
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }










    }

}