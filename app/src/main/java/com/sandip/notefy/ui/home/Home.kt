package com.sandip.notefy.ui.home

import android.app.Dialog
import android.content.res.Configuration
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.databinding.FragmentHomeBinding
import com.sandip.notefy.ui.MainActivity
import com.sandip.notefy.util.SortOrder
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home), NoteAdapter.OnItemClickListener{

    private val viewModel: HomeViewModel by viewModels()
    val noteAdapter = NoteAdapter(this)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentHomeBinding.bind(view)

        val profile_photo = view.findViewById<ImageView>(R.id.profile_photo)
        val drawerLayout = MainActivity.drawerLayout

        profile_photo.setOnClickListener {
            drawerLayout?.openDrawer(Gravity.LEFT)
        }


        binding.apply {
            recyclerView.apply {
                adapter = noteAdapter
                layoutManager =
                    if (requireActivity().resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                        StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                    } else {
                        StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
                    }

//                val pendingQuery = viewModel.searchQuery.value
//                if (pendingQuery != null && pendingQuery.isNotEmpty()) {
//                    searchView.setQuery(pendingQuery, false)
//                }

                searchView.setOnQueryTextListener(object :
                    android.widget.SearchView.OnQueryTextListener,
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        if (query != null) {
                            getItemsFromDb(query)
                        }
                        return true
                    }

                })



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
                        val task = noteAdapter.currentList[viewHolder.adapterPosition]
                        viewModel.onTaskSwiped(task)
                    }
                }).attachToRecyclerView(recyclerView)

                newNote.setOnClickListener {
                    viewModel.onAddNewTaskClick()
                }
                sortBy.setOnClickListener {
                    val dialog = Dialog(requireContext())
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    dialog.setContentView(R.layout.sortby_dialog)
                    dialog.show()
                    dialog.window?.setLayout(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                    )
//                    dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
                    dialog.window?.setGravity(Gravity.BOTTOM)
                    val bookmarked: LinearLayout = dialog.findViewById(R.id.bookmarked)
                    val titleName: LinearLayout = dialog.findViewById(R.id.title_name)
                    val newest: LinearLayout = dialog.findViewById(R.id.newest)
                    val oldest: LinearLayout = dialog.findViewById(R.id.oldest)
                    val image1: ImageView = dialog.findViewById(R.id.image1)
                    val image2: ImageView = dialog.findViewById(R.id.image2)
                    val image3: ImageView = dialog.findViewById(R.id.image3)
                    val image4: ImageView = dialog.findViewById(R.id.image4)

                    bookmarked.setOnClickListener {
                        image1.setImageResource(R.drawable.ic_baseline_done_24)
                        image2.setImageResource(0)
                        image3.setImageResource(0)
                        image4.setImageResource(0)
                        viewModel.onSortOrderSelected(SortOrder.BOOKMARKED)
                    }
                    titleName.setOnClickListener {
                        image2.setImageResource(R.drawable.ic_baseline_done_24)
                        image1.setImageResource(0)
                        image3.setImageResource(0)
                        image4.setImageResource(0)
                        viewModel.onSortOrderSelected(SortOrder.TITLE_ASC)
                    }
                    newest.setOnClickListener {
                        image3.setImageResource(R.drawable.ic_baseline_done_24)
                        image1.setImageResource(0)
                        image2.setImageResource(0)
                        image4.setImageResource(0)
                        viewModel.onSortOrderSelected(SortOrder.NEW_TO_OLD)
                    }
                    oldest.setOnClickListener {
                        image4.setImageResource(R.drawable.ic_baseline_done_24)
                        image1.setImageResource(0)
                        image2.setImageResource(0)
                        image3.setImageResource(0)
                        viewModel.onSortOrderSelected(SortOrder.OLD_TO_NEW)
                    }
                }
            }
            setFragmentResultListener("add_edit_delete_request") { _, bundle ->
                val result = bundle.getInt("add_edit_delete_result")
                viewModel.onAddEditResult(result)
            }


            viewModel.note.observe(viewLifecycleOwner) {
                noteAdapter.submitList(it)
            }

        }



        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {

                    is HomeViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                "note",
                                null,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                "edit note",
                                event.noteEntity,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), "Task deleted", Snackbar.LENGTH_LONG)
                            .setAction("UNDO") {
                                viewModel.onUndoDeleteClick(event.noteEntity)
                            }.show()
                    }
                    is HomeViewModel.TasksEvent.NavigateToDrawer -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                "note",
                                null,
                            )
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }
    }

    override fun onItemClick(noteEntity: NoteEntity) {
        viewModel.onTaskSelected(noteEntity)
    }

private fun getItemsFromDb(query: String) {

    viewModel.searchDatabase(query).observe(this) { list ->
        list?.let {
            noteAdapter.submitList(it)
        }

    }
}
}