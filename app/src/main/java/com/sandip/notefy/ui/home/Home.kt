package com.sandip.notefy.ui.home

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
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
import com.sandip.notefy.ui.newupdate.NewUpdateNote.Companion.cancelAlarm
import com.sandip.notefy.util.SortOrder
import com.sandip.notefy.util.exhaustive
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class Home : Fragment(R.layout.fragment_home), NoteAdapter.OnItemClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener
{
    private val viewModel: HomeViewModel by viewModels()
    private var gridLayoutManager: StaggeredGridLayoutManager? = null
    private var binding: FragmentHomeBinding? = null
    private var drawerLayout : DrawerLayout? = null
    private lateinit var bookmarked: RadioButton
    private lateinit var titleAsc: RadioButton
    private lateinit var titleDsc: RadioButton
    private lateinit var newest: RadioButton
    private lateinit var oldest: RadioButton
    private lateinit var radioGroup: RadioGroup
    private lateinit var dialog: Dialog
    private var homeActionMode : ActionMode ?  = null
    private lateinit var drawerListener : DrawerLayout.DrawerListener

    companion object{
        lateinit var noteList: List<NoteEntity>
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)
        Log.d("TAG","onCreate home")
        initSortByDialog()
        val noteAdapter = NoteAdapter(requireActivity(),this)
        viewModel.gridSharedPreferences.registerOnSharedPreferenceChangeListener(this)
        val prof = view.findViewById<ImageView>(R.id.profile_photo)

        gridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        binding?.apply {
            recyclerView.apply {
                adapter = noteAdapter
                layoutManager = observeGridLayout()
            }

            gridView.setOnCheckedChangeListener { _, isChecked ->
                viewModel.onGridViewToggle(isChecked)
            }

            viewModel.note.observe(viewLifecycleOwner) {
                noteAdapter.submitList(it)
                noteList = it
            }

            viewModel.displayUser.observe(viewLifecycleOwner) {
                if (it != null && !(it.image.isNullOrEmpty())) {
                    val imageUri = Uri.parse(it.image)
                    Glide.with(requireContext()).load(imageUri).into(prof)
                }
            }


            swipeRefreshLayout.setOnRefreshListener {
                viewModel.note.observe(viewLifecycleOwner) {
                    noteAdapter.submitList(it)
                    noteList = it
                }
                swipeRefreshLayout.isRefreshing = false
            }

            viewModel.isChecked.asLiveData().observe(viewLifecycleOwner) {
                val savedCheckedRadioButton =
                    radioGroup.getChildAt(it) as RadioButton
                savedCheckedRadioButton.isChecked = true
            }

            viewModel.noteCount.observe(viewLifecycleOwner) {
                if (it == 0) {
                    emptyNotes.emptyNotesError.visibility = View.VISIBLE
                } else {
                    emptyNotes.emptyNotesError.visibility = View.GONE
                }
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
                    cancelAlarm(context, task.requestCode)
                    viewModel.onTaskSwiped(task)
                    if(homeActionMode != null){
                        homeActionMode!!.finish()
                    }
                }
            }).attachToRecyclerView(recyclerView)

            newNote.setOnClickListener {
                viewModel.onAddNewTaskClick()
            }

            setFragmentResultListener("add_edit_delete_request") { _, bundle ->
                val result = bundle.getInt("add_edit_delete_result")
                context?.let { viewModel.onAddEditResult(it,result) }
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
                                null,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.NavigateToEditTaskScreen -> {
                        val action =
                            HomeDirections.actionHomeToNewUpdateNote(
                                event.noteEntity,
                            )
                        findNavController().navigate(action)
                    }
                    is HomeViewModel.TasksEvent.ShowTaskSavedConfirmationMessage -> {
                        Snackbar.make(view, event.msg, Snackbar.LENGTH_LONG)
                            .show()
                    }
                    is HomeViewModel.TasksEvent.ShowUndoDeleteTaskMessage -> {
                        Snackbar.make(view, getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                            .setAction(getString(R.string.undo)) {
                                viewModel.onUndoDeleteClick(event.noteEntity)
                            }.show()
                    }
                    is HomeViewModel.TasksEvent.NavigateToUserScreen -> {
                        val action =
                            HomeDirections.actionHomeToUser()
                        findNavController().navigate(action)
                    }
                    else -> {}
                }.exhaustive
            }
        }
    }

    private fun observeGridLayout(): StaggeredGridLayoutManager? {
        binding?.apply {
            gridLayoutManager?.apply {
                gridView.apply {
                    when (viewModel.gridSharedPreferences.getBoolean("grid", false)) {
                        true -> when (context?.resources?.configuration?.orientation) {
                            Configuration.ORIENTATION_PORTRAIT -> {
                                spanCount = 1
                                isChecked = true
                            }
                            Configuration.ORIENTATION_LANDSCAPE -> {
                                spanCount = 1
                                isChecked = true
                            }
                            else -> { gridLayoutManager }
                        }
                        false -> when (context?.resources?.configuration?.orientation) {
                            Configuration.ORIENTATION_PORTRAIT -> {
                                spanCount = 2
                                isChecked = false
                            }
                            Configuration.ORIENTATION_LANDSCAPE -> {
                                spanCount = 4
                                isChecked = false
                            }
                            else -> { gridLayoutManager }
                        }
                    }
                }
            }
        }
        return gridLayoutManager
    }

    override fun onItemClick(noteEntity: NoteEntity) {
        viewModel.onTaskSelected(noteEntity)
    }

    override fun onDeleteClick(noteEntity: NoteEntity) {
        viewModel.onMenuTaskDelete(noteEntity)
    }

    override fun onUndo(noteEntity: NoteEntity) {
        viewModel.onUndoDeleteClick(noteEntity)
    }

    override fun storeActionMode(mode: ActionMode?) {
        homeActionMode = mode
    }

    private var radioGroupOnCheckedChangeListener: RadioGroup.OnCheckedChangeListener =
        RadioGroup.OnCheckedChangeListener { _, checkedId ->
            val checkedRadioButton = radioGroup.findViewById<View>(checkedId) as RadioButton
            val checkedIndex = radioGroup.indexOfChild(checkedRadioButton)
            viewModel.updateSortOrderIsChecked(checkedIndex)
        }

    private fun initSortByDialog() {
        dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        dialog.apply {
            setContentView(R.layout.sortby_dialog)
            window?.attributes?.windowAnimations = R.style.DialogAnimation
            window?.setGravity(Gravity.BOTTOM)
            bookmarked = findViewById(R.id.bookmarked)
            titleAsc = findViewById(R.id.title_asc)
            titleDsc = findViewById(R.id.title_desc)
            newest = findViewById(R.id.newest)
            oldest = findViewById(R.id.oldest)
            radioGroup = findViewById(R.id.radioGroup)
            radioGroup.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener)
        }
    }

    override fun onStart() {
        super.onStart()
        drawerLayout = activity?.findViewById(R.id.drawer_layout)
        drawerListener =
            object : DrawerLayout.DrawerListener {
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerOpened(drawerView: View) {}
                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {
                    if(homeActionMode != null){
                        homeActionMode?.finish()
                        homeActionMode = null
                    }
                }
            }
        drawerLayout?.addDrawerListener(drawerListener)
    }

    override fun onPause() {
        super.onPause()
        if (homeActionMode != null) {
            homeActionMode?.finish()
            homeActionMode = null
        }
        drawerLayout?.removeDrawerListener(drawerListener)
        drawerLayout = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
        gridLayoutManager = null
        viewModel.gridSharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if(key.equals("grid")){
            observeGridLayout()
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        observeGridLayout()
    }
}
