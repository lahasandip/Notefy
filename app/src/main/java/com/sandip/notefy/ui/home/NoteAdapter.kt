package com.sandip.notefy.ui.home


import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.data.model.Todo
import com.sandip.notefy.databinding.NewNoteBinding
import com.sandip.notefy.ui.home.Home.Companion.act
import com.sandip.notefy.ui.home.Home.Companion.noteList
import com.sandip.notefy.ui.recycle_bin.RecycleBin
import java.text.SimpleDateFormat


class NoteAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback()) {
    var isEnable: Boolean = false
    var isSelectAll = false
    var selectList: ArrayList<NoteEntity> = ArrayList()
    var undoList: ArrayList<NoteEntity> = ArrayList()

    private lateinit var task :NoteEntity
    companion object {
        var homeActionMode : ActionMode ? = null
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NewNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(holder, currentItem)
    }

    inner class NoteViewHolder(private val binding: NewNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val rootView: View = act.window.decorView
            .findViewById(android.R.id.content)

        fun bind(holder: NoteViewHolder, noteEntity: NoteEntity) {
            binding.apply {
                overlay.setOnClickListener {
                    if (isEnable) {
                        clickItem(binding, holder)
                    } else {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val task = getItem(position)
                            listener.onItemClick(task)
                        }
                    }
                }
                overlay.setOnLongClickListener {

                    if (!isEnable) {
                        val callback = object : ActionMode.Callback {

                            override fun onCreateActionMode(
                                mode: ActionMode?,
                                menu: Menu?
                            ): Boolean {
                                mode?.menuInflater?.inflate(R.menu.home_contextual_action_bar, menu)
                                homeActionMode = mode
                                return true
                            }

                            override fun onPrepareActionMode(
                                mode: ActionMode?,
                                menu: Menu?
                            ): Boolean {
                                isEnable = true
                                clickItem(binding, holder)
                                (act as LifecycleOwner?)?.let { it1 ->
                                    HomeViewModel.mutableLiveData.observe(
                                        it1
                                    ) { s ->
                                        if (!(s.equals("0"))) {
                                            mode?.title = String.format("%s", s)
                                        } else {
                                            mode?.finish()
                                        }
                                    }
                                }
                                return false
                            }

                            override fun onActionItemClicked(
                                mode: ActionMode?,
                                item: MenuItem?
                            ): Boolean {
                                return when (item?.itemId) {

                                    R.id.select -> {

                                        item.icon = ContextCompat.getDrawable(NotefyApplication.appContext, R.drawable.ic_baseline_deselect_24)
                                        if(selectList.size == noteList.size)
                                        {
                                            isSelectAll=false
                                            selectList.clear()
                                        }
                                        else
                                        {
                                            isSelectAll=true
                                            selectList.clear()
                                            selectList.addAll(noteList)

                                        }
                                        HomeViewModel.mutableLiveData.value = selectList.size.toString()
                                        notifyDataSetChanged()
                                        true
                                    }
                                    R.id.delete -> {
                                        if(undoList.size != 0){
                                            undoList.clear()
                                        }
                                        for (s in selectList) {
                                            undoList.add(s)
                                            listener.onDeleteClick(s)
                                        }

                                        Snackbar.make(rootView, act.getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                                            .setAction(act.getString(R.string.undo)) {
                                                for (s in undoList) {
                                                    listener.onUndo(s)
                                                }
                                            }.show()
                                        mode?.finish()
                                        true
                                    }
                                    else -> false
                                }
                            }

                            override fun onDestroyActionMode(mode: ActionMode?) {
                                isEnable=false
                                isSelectAll=false
                                selectList.clear()
                                notifyDataSetChanged()
                            }
                        }
                        act.startActionMode(callback)
                    }
                    else
                    {
                        clickItem(binding, holder)
                    }
                    true
                }

                if(isSelectAll)
                {
                    binding.cardView.strokeWidth = 8
                    binding.cardView.strokeColor = Color.parseColor("#80cbc4")
                }
                else
                { binding.cardView.strokeWidth = 0
                    binding.cardView.strokeColor = Color.parseColor("#9e9e9e")
                }

                important.isChecked = noteEntity.important

                noteTitle.text = noteEntity.title
                if (!(noteEntity.body.isNullOrEmpty())) {
                    noteBody.text = noteEntity.body
                    noteBody.visibility = View.VISIBLE
                }
                if (!(noteEntity.url.isNullOrEmpty())) {
                    urlLink.text = noteEntity.url
                    layoutURL.visibility = View.VISIBLE
                }
                if ((!(noteEntity.dateTime.isNullOrEmpty()))
                ) {
                    var date2 = noteEntity.dateTime
                    var spf = SimpleDateFormat("yyyy-MM-dd-h:m")
                    val newDate = spf.parse(date2)
                    spf = SimpleDateFormat("MMM d, ''yy, h:m")
                    date2 = newDate?.let { spf.format(it) }
                    dateTime.text = date2
                    layoutDate.visibility = View.VISIBLE
                    if (noteEntity.isStriked) {
                        dateTime.paintFlags =
                            dateTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                }
                if (!(noteEntity.location.isNullOrEmpty())) {
                    location.text = noteEntity.location
                    layoutLocation.visibility = View.VISIBLE

                }
                cardView.setCardBackgroundColor(noteEntity.clr)

                if(noteEntity.image != null) {
                    val imageUri = Uri.parse(noteEntity.image)
                    Glide.with(NotefyApplication.appContext).load(imageUri).into(img)
                    noteImageLayout.visibility = View.VISIBLE
                }
                else{
                    Glide.with(NotefyApplication.appContext).clear(img)
                    noteImageLayout.visibility = View.GONE
                }

                if (noteEntity.todoList != null) {
                    val todoAdapter = HomeTodoAdapter(NotefyApplication.appContext,noteEntity.todoList as ArrayList<Todo>)
                    todoRecyclerView.setHasFixedSize(true)
                    todoRecyclerView.layoutManager = LinearLayoutManager(NotefyApplication.appContext)
                    todoRecyclerView.adapter = todoAdapter
                    todoRecyclerView.visibility = View.VISIBLE
                    todoAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    private fun clickItem(binding: NewNoteBinding, holder: NoteViewHolder) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            task = getItem(position)
        }
        if (binding.cardView.strokeWidth == 0) {
            binding.cardView.strokeWidth = 8
            binding.cardView.strokeColor = Color.parseColor("#80cbc4")
            selectList.add(task)
        } else {
            binding.cardView.strokeWidth = 0
            binding.cardView.strokeColor = Color.parseColor("#9e9e9e")
            selectList.remove(task)
        }
        HomeViewModel.mutableLiveData.value = selectList.size.toString()
    }

    interface OnItemClickListener {
        fun onItemClick(noteEntity: NoteEntity)
        fun onDeleteClick(noteEntity: NoteEntity)
        fun onUndo(noteEntity: NoteEntity)
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}