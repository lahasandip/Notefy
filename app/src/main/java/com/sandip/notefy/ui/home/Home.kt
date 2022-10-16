package com.sandip.notefy.ui.home

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.R
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.databinding.FragmentHomeBinding
import com.sandip.notefy.ui.MainActivity
import com.sandip.notefy.ui.home.NoteAdapter.Companion.homeActionMode
import com.sandip.notefy.util.SortOrder
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home), NoteAdapter.OnItemClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: HomeViewModel by viewModels()
    private val noteAdapter = NoteAdapter(this)
    private lateinit var gridLayoutManager: StaggeredGridLayoutManager
    private lateinit var binding: FragmentHomeBinding
    private lateinit var bookmarked: RadioButton
    private lateinit var titleAsc: RadioButton
    private lateinit var titleDsc: RadioButton
    private lateinit var newest: RadioButton
    private lateinit var oldest: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var dialog: Dialog
    companion object{
        lateinit var act : Activity
        lateinit var noteList: List<NoteEntity>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        val drawerLayout = MainActivity.drawerLayout
        displaySortByDialog()

        act = requireActivity()



        val prof = view.findViewById<ImageView>(R.id.profile_photo)

        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)

        binding.apply {
            recyclerView.apply {
                adapter = noteAdapter
                layoutManager = observeGridLayout()
//                setHasFixedSize(true)


                gridView.setOnCheckedChangeListener { _, isChecked ->
                    val sharedPreferences =
                        context.getSharedPreferences("grid", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putBoolean("grid", isChecked)
                    editor.apply()
                }

                viewModel.displayUser.observe(viewLifecycleOwner) {
                    if (it != null) {
                        if (!(it.image.isNullOrEmpty())) {
                            val imageUri = Uri.parse(it.image)
                            this.let { it1 -> Glide.with(it1).load(imageUri).into(prof) }
                        }
                    }
                }


                swipeRefreshLayout.setOnRefreshListener {

                    viewModel.note.observe(viewLifecycleOwner) {
                        noteAdapter.submitList(it)
                    }
                    swipeRefreshLayout.isRefreshing = false

                }
                viewModel.isChecked.asLiveData().observe(viewLifecycleOwner) {
                    val savedCheckedRadioButton =
                        radioGroup.getChildAt(it) as RadioButton
                    savedCheckedRadioButton.isChecked = true
                }



                searchView.setOnQueryTextListener(object :
                    android.widget.SearchView.OnQueryTextListener,
                    SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String?): Boolean {
                        return true
                    }

                    override fun onQueryTextChange(query: String?): Boolean {
                        if (query != null) {
                            viewModel.searchQuery.value = query
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
                        viewModel.onTaskSwiped(task, true)
                        if(homeActionMode != null){
                            homeActionMode!!.finish()
                        }
                    }
                }).attachToRecyclerView(recyclerView)
            }
            newNote.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }


            setFragmentResultListener("add_edit_delete_request") { _, bundle ->
                val result = bundle.getInt("add_edit_delete_result")
                context?.let { viewModel.onAddEditResult(it,result) }
            }

            viewModel.note.observe(viewLifecycleOwner) {
                noteAdapter.submitList(it)
                noteList = it
                Log.d("live data", it.toString())
            }

            viewModel.noteCount.observe(viewLifecycleOwner) {
                if (it == 0) {
                    emptyNotes.emptyNotesError.visibility = View.VISIBLE
                } else {
                    emptyNotes.emptyNotesError.visibility = View.GONE
                }
            }

            topAppBar.setNavigationOnClickListener {
                drawerLayout?.openDrawer(GravityCompat.START)
            }



            prof.setOnClickListener {
                viewModel.onProfileClick()
            }

            topAppBar.setOnMenuItemClickListener { menuItem ->

                when (menuItem.itemId) {

                    R.id.sort -> {
                        dialog.show()
                        bookmarked.setOnClickListener {
                            viewModel.onSortOrderSelected(SortOrder.BOOKMARKED)

                        }
                        titleAsc.setOnClickListener {
                            viewModel.onSortOrderSelected(SortOrder.TITLE_ASC)
                        }
                        titleDsc.setOnClickListener {
                            viewModel.onSortOrderSelected(SortOrder.TITLE_DSC)
                        }
                        newest.setOnClickListener {
                            viewModel.onSortOrderSelected(SortOrder.NEW_TO_OLD)
                        }
                        oldest.setOnClickListener {
                            viewModel.onSortOrderSelected(SortOrder.OLD_TO_NEW)
                        }
                        true
                    }
                    else -> false
                }
            }
        }



        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.tasksEvent.collect { event ->
                when (event) {
                    is HomeViewModel.TasksEvent.NavigateToAddTaskScreen -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                false,
                                null,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                false,
                                event.noteEntity,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(requireView(), event.msg, Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.TasksEvent.ShowDeletedTaskMessage -> {
                        Snackbar.make(requireView(), event.text, Snackbar.LENGTH_SHORT).show()
                    }
                    is HomeViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(requireView(), getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDeleteClick(event.noteEntity)
                            }.show()
                    }
                    is HomeViewModel.TasksEvent.NavigateToDrawer -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                false,
                                null,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.NavigateToUserScreen -> {
                        val action =
                            HomeDirections.actionHomeToUser()
                        findNavController().navigate(action)
                    }
                }.exhaustive
            }
        }

        observeLanguagePreference()

    }

    private fun observeGridLayout(): RecyclerView.LayoutManager {
        val sharedPreferences = context?.getSharedPreferences("grid", Context.MODE_PRIVATE)
        sharedPreferences?.registerOnSharedPreferenceChangeListener(this)

        when (sharedPreferences?.getBoolean("grid", false)) {

            true -> if (context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager.spanCount = 1
                binding.gridView.isChecked = true

            } else {
                gridLayoutManager.spanCount = 1
                binding.gridView.isChecked = true
            }
            false -> if (context?.resources?.configuration?.orientation == Configuration.ORIENTATION_PORTRAIT) {
                gridLayoutManager.spanCount = 2
                binding.gridView.isChecked = false

            } else {
                gridLayoutManager.spanCount = 4
                binding.gridView.isChecked = false
            }
            else -> {
                gridLayoutManager
            }
        }
        return gridLayoutManager
    }

    override fun onItemClick(noteEntity: NoteEntity) {
        viewModel.onTaskSelected(noteEntity)
    }

    override fun onDeleteClick(noteEntity: NoteEntity) {
        viewModel.onMenuTaskDelete(noteEntity, true)
    }

    override fun onUndo(noteEntity: NoteEntity) {
        viewModel.onUndoDeleteClick(noteEntity)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key.equals("grid")) {
            observeGridLayout()
        }
    }

    private var radioGroupOnCheckedChangeListener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<View>(checkedId) as RadioButton
            val checkedIndex = radioGroup.indexOfChild(checkedRadioButton)
            viewModel.updateSortOrderIsChecked(checkedIndex)
        }

    private fun displaySortByDialog() {
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.setContentView(R.layout.sortby_dialog)
        dialog.window?.attributes?.windowAnimations = R.style.DialogAnimation
        dialog.window?.setGravity(Gravity.BOTTOM)
        bookmarked = dialog.findViewById(R.id.bookmarked)
        titleAsc = dialog.findViewById(R.id.title_asc)
        titleDsc = dialog.findViewById(R.id.title_desc)
        newest = dialog.findViewById(R.id.newest)
        oldest = dialog.findViewById(R.id.oldest)

        radioGroup = dialog.findViewById(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener)
    }

    private fun observeLanguagePreference() {
        val sharedPreferences =  context?.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        val position = sharedPreferences?.getInt("position", 0)
        if (position != null) {
            viewModel.onTaskSelected(context, position)
        }
    }

    override fun onPause() {
        super.onPause()
        if (homeActionMode != null) {
            homeActionMode?.finish()
            homeActionMode = null
        }
    }
}
