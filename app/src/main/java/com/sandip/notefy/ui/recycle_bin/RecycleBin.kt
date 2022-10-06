package com.sandip.notefy.ui.recycle_bin

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.databinding.FragmentRecycleBinBinding
import com.sandip.notefy.ui.home.HomeViewModel
import com.sandip.notefy.ui.recycle_bin.RecycleAdapter.Companion.recycleActionMode
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RecycleBin : Fragment(R.layout.fragment_recycle_bin), RecycleAdapter.OnItemClickListener {

    private val viewModel: RecycleBinViewModel by viewModels()

    private lateinit var binding: FragmentRecycleBinBinding
    private lateinit var recycleAdapter: RecycleAdapter
    companion object {
        lateinit var noteList: List<NoteEntity>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecycleBinBinding.bind(view)
        recycleAdapter = RecycleAdapter(this)

        binding.apply {
            trashRecyclerView.apply {
                adapter = recycleAdapter
                layoutManager =
                    if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    } else {
                        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                    }
            }
            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
                0,
                ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
            ) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = recycleAdapter.currentList[viewHolder.adapterPosition]
                    viewModel.onTaskSwiped(task)
                    if(recycleActionMode != null){
                        recycleActionMode!!.finish()
                    }
                }
            }).attachToRecyclerView(trashRecyclerView)


            viewModel.note.observe(viewLifecycleOwner){
                recycleAdapter.submitList(it)
                if(it.isNullOrEmpty()){
                    emptyRecycleBin.emptyRecycleBinError.visibility = View.VISIBLE
                }
                else{
                    emptyRecycleBin.emptyRecycleBinError.visibility = View.GONE
                }
                noteList = it
            }
            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.tasksEvent.collect { event ->
                    when (event) {
                        is RecycleBinViewModel.TasksEvent.NavigateToBackScreen -> {
                            findNavController().popBackStack()
                        }
                        is RecycleBinViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                            Snackbar.make(requireView(), "Note Deleted Permanently", Snackbar.LENGTH_LONG)
                                .setAction("UNDO") {
                                    viewModel.onUndoDeleteClick(event.noteEntity)
                                }.show()
                        }
                        is RecycleBinViewModel.TasksEvent.ShowDeletedTaskMessage -> {
                            Snackbar.make(requireView(), event.s, Snackbar.LENGTH_SHORT).show()
                        }
                    }.exhaustive
                }
            }



        }

    }

    override fun onItemClick(noteEntity: NoteEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle("Restore")
            .setMessage("Do you want to restore the note?")
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Yes") { _, _ ->
                viewModel.onMenuRestore(noteEntity, false)
                Snackbar.make(requireView(), "Note Restored", Snackbar.LENGTH_LONG).show()
            }
            .create()
            .show()
    }

    override fun onRestoreClick(noteEntity: NoteEntity) {
        viewModel.onMenuRestore(noteEntity, false)
    }

    override fun onMenuDeleteClick(noteEntity: NoteEntity) {
        viewModel.onMenuDelete(noteEntity)

    }

    override fun onUndo(noteEntity: NoteEntity) {
        viewModel.onUndoDeleteClick(noteEntity)
    }
}