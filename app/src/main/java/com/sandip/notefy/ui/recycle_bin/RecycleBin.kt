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
import com.sandip.notefy.ui.recycle_bin.RecycleAdapter.Companion.recycleActionMode
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class RecycleBin : Fragment(R.layout.fragment_recycle_bin), RecycleAdapter.OnItemClickListener {

    private val viewModel: RecycleBinViewModel by viewModels()
    private lateinit var binding: FragmentRecycleBinBinding
    companion object {
        lateinit var noteList: List<NoteEntity>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecycleBinBinding.bind(view)
        val recycleAdapter = RecycleAdapter(requireActivity(),view,this)

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
                            Snackbar.make(requireView(), getString(R.string.notes_deleted_forever), Snackbar.LENGTH_LONG)
                                .setAction(getString(R.string.undo)) {
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
            .setTitle(getString(R.string.restore))
            .setMessage(getString(R.string.do_you_want_to_restore_the_note))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.onMenuRestore(context, noteEntity, false)
            }
            .create()
            .show()
    }

    override fun onRestoreClick(noteEntity: NoteEntity) {
        viewModel.onMenuRestore(context, noteEntity, false)
    }

    override fun onMenuDeleteClick(noteEntity: NoteEntity) {
        viewModel.onMenuDelete(noteEntity)
    }

    override fun onUndo(noteEntity: NoteEntity) {
        viewModel.onUndoDeleteClick(noteEntity)
    }
}