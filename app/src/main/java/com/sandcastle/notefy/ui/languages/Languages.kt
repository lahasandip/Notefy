package com.sandcastle.notefy.ui.languages

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.model.Language
import com.sandcastle.notefy.databinding.FragmentLanguagesBinding
import com.sandcastle.notefy.util.ModeManager.observeLanguagePreference
import com.sandcastle.notefy.util.exhaustive

class Languages : Fragment(R.layout.fragment_languages), LanguagesAdapter.OnItemClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener
{
    private val viewModel: LanguagesViewModel by viewModels()
    private var binding: FragmentLanguagesBinding? = null
    private var gridLayoutManager: StaggeredGridLayoutManager? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)
        viewModel.languageSharedPreferences.registerOnSharedPreferenceChangeListener(this )

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
        val languagesAdapter = LanguagesAdapter(context, languageList, this)

        gridLayoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)

        binding?.apply {
            languageRecyclerView.apply {
                adapter = languagesAdapter
                layoutManager = observeGridLayout()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is LanguagesViewModel.TasksEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }

            topAppBar.setNavigationOnClickListener {
                viewModel.onContinueBackClick()
            }

            ok.setOnClickListener {
                viewModel.onContinueBackClick()
            }
        }
    }
    private fun observeGridLayout(): StaggeredGridLayoutManager? {
        binding?.apply {
            gridLayoutManager?.apply {
                when (context?.resources?.configuration?.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        spanCount = 3
                    }
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        spanCount = 5
                    }
                    else -> { gridLayoutManager
                    }
                }
            }
        }
        return gridLayoutManager
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        observeGridLayout()
    }

    override fun onItemClick(flag: Int) {
        viewModel.onGridViewToggle(flag)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("position"))  {
            observeLanguagePreference(requireContext())
            activity?.recreate()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gridLayoutManager = null
        binding = null
        viewModel.languageSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
}
