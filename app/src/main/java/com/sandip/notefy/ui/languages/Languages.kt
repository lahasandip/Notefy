package com.sandip.notefy.ui.languages

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.app.ActivityCompat.recreate
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.internal.ContextUtils
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Language
import com.sandip.notefy.databinding.FragmentLanguagesBinding
import com.sandip.notefy.ui.home.HomeDirections
import com.sandip.notefy.ui.home.HomeViewModel
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint
import kotlin.collections.ArrayList

class Languages : Fragment(R.layout.fragment_languages), LanguagesAdapter.OnItemClickListener  {

    private val viewModel: LanguagesViewModel by viewModels()
    private lateinit var binding: FragmentLanguagesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)

        val flagImages = intArrayOf(
            R.drawable.usa,
            R.drawable.india,
            R.drawable.spain,
            R.drawable.bangladesh,
            R.drawable.france,
            R.drawable.china,
            R.drawable.india,
            R.drawable.portugese,
            R.drawable.indonesia,
            R.drawable.japan,
            R.drawable.india,
            R.drawable.russia,
            R.drawable.india,
            R.drawable.turkey,
            R.drawable.italy

        )
        val language = arrayOf(
            "English", "हिन्दी", "Española", "বাংলা", "Français", "中国人", "தமிழ்",
            "Português", "bahasa Indonesia", "日本", "తెలుగు", "Русский", "मराठी", "Türk", "Italiana"
        )
        val languageList = ArrayList<Language>()

        for (i in language.indices) {

            val languagesClass = Language(
                flagImages[i], language[i]
            )
            languageList.add(languagesClass)

        }
        val languagesAdapter = LanguagesAdapter(languageList, this)



        binding.apply {
            languageRecyclerView.apply {
                adapter = languagesAdapter
                layoutManager =
                    if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
                    } else {
                        StaggeredGridLayoutManager(5, StaggeredGridLayoutManager.VERTICAL)
                    }
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is LanguagesViewModel.TasksEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is LanguagesViewModel.TasksEvent.NavigateToHomeScreen -> {

                            val action =
                                LanguagesDirections.actionLanguagesToHome()
                            findNavController().navigate(action)
                        }
                    }.exhaustive
                }
            }

            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }

            ok.setOnClickListener {
                viewModel.onContinueClick()
                activity?.let { it1 ->
                    recreate(it1)


//                val intent = requireActivity().intent
//                intent.addFlags(
//                    Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
//                            or Intent.FLAG_ACTIVITY_NO_ANIMATION
//                )
//                requireActivity().overridePendingTransition(0, 0)
//                requireActivity().finish()
//
//                requireActivity().overridePendingTransition(0, 0)
//                startActivity(intent)

                }

            }
        }
    }


    override fun onItemClick(position: Int) {
//        viewModel.onTaskSelected(context, flag)
//        activity?.let { it1 -> recreate(it1)
//        }
        val sharedPreferences =  context?.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        val editor = sharedPreferences?.edit()
        editor?.putInt("position",position)
        editor?.commit()
//        viewModel.savePreference(position)

    }

}
