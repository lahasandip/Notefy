package com.sandip.notefy.ui.languages

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sandip.notefy.R
import com.sandip.notefy.data.Language
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.databinding.FragmentLanguagesBinding
import com.sandip.notefy.util.exhaustive

class Languages : Fragment(R.layout.fragment_languages), LanguagesAdapter.OnItemClickListener {

    private val viewModel: LanguagesViewModel by viewModels()
    private lateinit var binding: FragmentLanguagesBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentLanguagesBinding.bind(view)

        val flagImages = intArrayOf(
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img,
            R.drawable.img

        )
        val language = arrayOf("english1", "Manchurian", "Sandwich", "Sandwich", "Sandwich","Sandwich","Sandwich",
            "Sandwich","Sandwich","Sandwich","Sandwich","Sandwich", "Sandwich", "Sandwich", "Sandwich")
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
//            ok.setOnClickListener {
//                viewModel.onOkClick()
//            }
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
//                        val action =
//                            LanguagesDirections.actionLanguagesToHome(
//                            )
//                        findNavController().navigate(action)
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }



    }


//            override fun onItemClick(noteEntity: NoteEntity) {
//                TODO("Not yet implemented")
//            }

        }

    override fun onItemClick(flag : Boolean) {
        viewModel.onTaskSelected(flag)

    }

}
