package com.sandip.notefy.ui.languages

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Language
import com.sandip.notefy.databinding.FragmentLanguagesBinding
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Languages : Fragment(R.layout.fragment_languages), LanguagesAdapter.OnItemClickListener  {

    private val viewModel: LanguagesViewModel by viewModels()
    private lateinit var binding: FragmentLanguagesBinding
//    var position = 0
    private var sharedPreferences : SharedPreferences? = null
    private var editor : SharedPreferences.Editor? = null
    //    companion object {
//        private var position = 0
//    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)

        sharedPreferences =  context?.getSharedPreferences("LANGUAGE", Context.MODE_PRIVATE)
        val position =  sharedPreferences?.getInt("position", 0)
        editor = sharedPreferences?.edit()


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
            val languagesClass = Language(flagImages[i], language[i])
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
//
//            viewModel.langPosition.asLiveData().observe(viewLifecycleOwner) {
//               position = it
//            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is LanguagesViewModel.TasksEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is LanguagesViewModel.TasksEvent.NavigateToHomeScreen -> {
                            activity?.recreate()
//                            val action =
//                                LanguagesDirections.actionLanguagesToHome()
//                            findNavController().navigate(action)
                            findNavController().popBackStack()
//                            val i = requireActivity().baseContext.packageManager
//                                .getLaunchIntentForPackage(requireActivity().baseContext.packageName)
//                            i?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                            startActivity(i)
                        }
                    }.exhaustive
                }
            }

            topAppBar.setNavigationOnClickListener {
                if (position != null) {
                    editor?.putInt("position",position)
                }
                editor?.apply()
                viewModel.onOkClick()
//                activity?.let { it1 ->
//                    recreate(it1)
//                }
            }

            ok.setOnClickListener {
                viewModel.onContinueClick()
//                activity?.let { it1 ->
//                    recreate(it1)
//                }
            }
        }
    }

    override fun onItemClick(flag: Int) {
        editor?.putInt("position",flag)
        editor?.apply()
//        viewModel.saveLangPos(flag)

    }
}
