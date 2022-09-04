package com.sandip.notefy.ui.todo

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.sandip.notefy.R
import com.sandip.notefy.databinding.FragmentHomeBinding
import com.sandip.notefy.databinding.TodoListviewBinding
import com.sandip.notefy.ui.home.NoteAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class Todo : Fragment(R.layout.todo_listview){

//    private var viewModel: TodoViewModel by viewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = TodoListviewBinding.bind(view)

//        val todoAdapter = TodoAdapter(this)
//
//        binding.apply {
//            todoRecyclerview.apply {
//                adapter = todoAdapter
//                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//            }
//
//
  }

}