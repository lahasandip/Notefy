package com.sandip.notefy.ui.languages

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.core.app.ActivityCompat.recreate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sandip.notefy.R
import com.sandip.notefy.data.Language
import com.sandip.notefy.databinding.FragmentLanguagesBinding
import com.sandip.notefy.util.exhaustive
import kotlin.collections.ArrayList

class Languages : Fragment(R.layout.fragment_languages), LanguagesAdapter.OnItemClickListener {

    private val viewModel: LanguagesViewModel by viewModels()
    private lateinit var binding: FragmentLanguagesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)
//        viewModel.readLanguageCode.observe(viewLifecycleOwner) {
//
//            when(it){
//            0 -> updateResource(context, "en")
//            1 -> updateResource(context, "bn")
//            2 -> updateResource(context, "bn")
//            3 -> updateResource(context, "bn")
//            4 -> updateResource(context, "bn")
//            5 -> updateResource(context, "bn")
//            6 -> updateResource(context, "bn")
//            7 -> updateResource(context, "bn")
//            8 -> updateResource(context, "bn")
//            9 -> updateResource(context, "bn")
//            10 -> updateResource(context, "bn")
//            11 -> updateResource(context, "bn")
//            12 -> updateResource(context, "bn")
//            13 -> updateResource(context, "bn")
//            14 -> updateResource(context, "bn")
//        }
//
//
//
//
//
//
//
//
//        }




        val flagImages = intArrayOf(
            R.drawable.usa,
            R.drawable.india,
            R.drawable.spain,
            R.drawable.india,
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
        val language = arrayOf("English", "हिन्दी", "Española", "বাংলা", "Français","中国人","தமிழ்",
            "Português","bahasa Indonesia","日本","తెలుగు","Русский", "मराठी", "Türk", "Italiana")
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
                    }.exhaustive
                }
            }

            back.setOnClickListener {
                viewModel.onOkClick()
//                viewModel.onTaskSelected(context,)
            }
            ok.setOnClickListener {
                viewModel.onOkClick()
            }
        }
    }


    override fun onItemClick(flag: Int) {
        viewModel.onTaskSelected(context, flag)
        activity?.let { it1 -> recreate(it1) }
    }

//    fun updateResource(context: Context?, s: String){
//        val locale = Locale(s)
//        Locale.setDefault(locale)
//
//        val configuration = Configuration()
//        configuration.locale = locale
//
//        context?.resources?.updateConfiguration(configuration, context
//            .resources.displayMetrics
//        );
//
//
//    }
}
