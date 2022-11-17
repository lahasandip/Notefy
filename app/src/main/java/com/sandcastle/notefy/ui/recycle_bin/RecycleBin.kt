package com.sandcastle.notefy.ui.recycle_bin

import android.content.res.Configuration
import android.os.Bundle
import android.view.ActionMode
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.entity.NoteEntity
import com.sandcastle.notefy.databinding.FragmentRecycleBinBinding
import com.sandcastle.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint

@Suppress("IMPLICIT_CAST_TO_ANY")
@AndroidEntryPoint
class RecycleBin : Fragment(R.layout.fragment_recycle_bin), RecycleAdapter.OnItemClickListener {

    private val viewModel: RecycleBinViewModel by viewModels()
    private var binding: FragmentRecycleBinBinding? = null
    private var recycleActionMode : ActionMode ?  = null
    private var drawerLayout : DrawerLayout? = null
    private lateinit var drawerListener : DrawerLayout.DrawerListener
    private var gridLayoutManager: StaggeredGridLayoutManager? = null
    companion object {
        lateinit var noteList: List<NoteEntity>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentRecycleBinBinding.bind(view)
        val recycleAdapter = RecycleAdapter(requireActivity(), this)
        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding?.apply {
            trashRecyclerView.apply {
                adapter = recycleAdapter
                layoutManager = observeGridLayout()
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
                    recycleActionMode?.finish()
                }
            }).attachToRecyclerView(trashRecyclerView)

            viewModel.note.observe(viewLifecycleOwner) {
                recycleAdapter.submitList(it)
                if (it.isNullOrEmpty()) {
                    emptyRecycleBin.emptyRecycleBinError.visibility = View.VISIBLE
                } else {
                    emptyRecycleBin.emptyRecycleBinError.visibility = View.GONE
                }
                noteList = it
            }
            topAppBar.setNavigationOnClickListener {
                viewModel.onOkClick()
            }
        }
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is RecycleBinViewModel.TasksEvent.NavigateToBackScreen -> {
                        findNavController().popBackStack()
                    }
                    is RecycleBinViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(view, getString(R.string.notes_deleted_forever), Snackbar.LENGTH_SHORT)
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDeleteClick(event.noteEntity)
                            }.show()
                    }
                    is RecycleBinViewModel.TasksEvent.ShowDeletedTaskMessage -> {
                        Snackbar.make(view, event.s, Snackbar.LENGTH_SHORT).show()
                    }
                }.exhaustive
            }
        }
    }

    private fun observeGridLayout(): StaggeredGridLayoutManager? {
        binding?.apply {
            gridLayoutManager?.apply {
                when (context?.resources?.configuration?.orientation) {
                    Configuration.ORIENTATION_PORTRAIT -> {
                        spanCount = 2
                    }
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        spanCount = 4
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

    override fun onItemClick(noteEntity: NoteEntity) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.restore))
            .setMessage(getString(R.string.do_you_want_to_restore_the_note))
            .setNegativeButton(getString(R.string.cancel), null)
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                viewModel.onMenuRestore(context, noteEntity)
            }
            .create()
            .show()
    }

    override fun onRestoreClick(noteEntity: NoteEntity) {
        viewModel.onMenuRestore(context, noteEntity)
    }

    override fun onMenuDeleteClick(noteEntity: NoteEntity) {
        viewModel.onMenuDelete(noteEntity)
    }

    override fun onUndo(noteEntity: NoteEntity) {
        viewModel.onUndoDeleteClick(noteEntity)
    }

    override fun storeActionMode(mode: ActionMode?) {
        recycleActionMode = mode
    }

    override fun onStart() {
        super.onStart()
        drawerLayout = activity?.findViewById(R.id.drawer_layout)
        drawerListener = object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerOpened(drawerView: View) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerStateChanged(newState: Int) {
                if(recycleActionMode != null){
                    recycleActionMode?.finish()
                    recycleActionMode = null
                }
            }
        }
        drawerLayout?.addDrawerListener(drawerListener)

    }
    override fun onPause() {
        super.onPause()
        if (recycleActionMode != null) {
            recycleActionMode?.finish()
            recycleActionMode = null
        }
        drawerLayout?.removeDrawerListener(drawerListener)
        drawerLayout = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        gridLayoutManager = null
        binding = null
    }
}