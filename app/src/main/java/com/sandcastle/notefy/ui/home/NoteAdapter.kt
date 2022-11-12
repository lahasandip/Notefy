package com.sandcastle.notefy.ui.home


import android.app.Activity
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.entity.NoteEntity
import com.sandcastle.notefy.data.model.Todo
import com.sandcastle.notefy.databinding.NewNoteBinding
import com.sandcastle.notefy.ui.home.Home.Companion.noteList
import com.sandcastle.notefy.ui.newupdate.NewUpdateNote.Companion.cancelAlarm
import com.sandcastle.notefy.util.Converters.Companion.getDateFormat

class NoteAdapter(activity: Activity, private val listener: OnItemClickListener) :
    ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback()) {
    private val mActivity = activity
    private var isEnable: Boolean = false
    private var isSelectAll = false
    private var selectList: ArrayList<NoteEntity> = ArrayList()
    private var undoList: ArrayList<NoteEntity> = ArrayList()
    private var mItem: MenuItem? = null
    private lateinit var task :NoteEntity
    private var mutableLiveData = MutableLiveData<String?>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        return NoteViewHolder(NewNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(holder, getItem(position))
    }

    inner class NoteViewHolder(private val binding: NewNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(holder: NoteViewHolder, noteEntity: NoteEntity) {
            binding.apply {
                overlay.setOnClickListener {
                    if (isEnable) {
                        clickItem(binding, holder)
                    } else {
                        if (adapterPosition != RecyclerView.NO_POSITION) {
                            listener.onItemClick(getItem(adapterPosition))
                        }
                    }
                }
                overlay.setOnLongClickListener {
                    if (!isEnable) {
                        val callback = object : ActionMode.Callback {
                            override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                mode?.menuInflater?.inflate(R.menu.home_contextual_action_bar, menu)
                                listener.storeActionMode(mode)
                                mItem = mode?.menu?.getItem(0)
                                return true
                            }
                            override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
                                isEnable = true
                                clickItem(binding, holder)
                                (mActivity as LifecycleOwner).let { it1 ->
                                    mutableLiveData.observe(it1) { s ->
                                        if (!(s.equals("0"))) {
                                            mode?.title = String.format("%s", s)
                                        } else {
                                            mode?.finish()
                                        }
                                    }
                                }
                                return false
                            }
                            override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
                                return when (item?.itemId) {
                                    R.id.select -> {
                                        if(selectList.size == noteList.size) {
                                            isSelectAll=false
                                            selectList.clear()
                                        }
                                        else{
                                            isSelectAll=true
                                            selectList.clear()
                                            selectList.addAll(noteList)
                                        }
                                        item.icon = ContextCompat.getDrawable(mActivity.applicationContext, R.drawable.ic_baseline_deselect_24)
                                        mutableLiveData.value = selectList.size.toString()
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
                                            cancelAlarm(mActivity.applicationContext, s.requestCode)
                                        }
                                        Snackbar.make(itemView, mActivity.getString(R.string.note_deleted), Snackbar.LENGTH_LONG)
                                            .setAction(mActivity.getString(R.string.undo)) {
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
                                mItem = null
                                mutableLiveData.removeObservers(mActivity as LifecycleOwner)
                                notifyDataSetChanged()
                            }
                        }
                        mActivity.startActionMode(callback)
                    }
                    else {
                        clickItem(binding, holder)
                    }
                    true
                }

                if(isSelectAll){
                    cardView.strokeWidth = 8
                    cardView.strokeColor = Color.parseColor("#80cbc4")
                }
                else { cardView.strokeWidth = 0
                    cardView.strokeColor = Color.parseColor("#9e9e9e")
                }

                important.isChecked = noteEntity.important
                noteTitle.text = noteEntity.title

                if (!(noteEntity.body.isNullOrEmpty())) {
                    noteBody.text = noteEntity.body
                    noteBody.visibility = View.VISIBLE
                }
                else{
                    noteBody.text = null
                    noteBody.visibility = View.GONE
                }
                if (!(noteEntity.url.isNullOrEmpty())) {
                    urlLink.text = noteEntity.url
                    layoutURL.visibility = View.VISIBLE
                }
                else{
                    urlLink.text = null
                    layoutURL.visibility = View.GONE
                }
                if ((!(noteEntity.dateTime.isNullOrEmpty()))) {
                    dateTime.text = getDateFormat(noteEntity.dateTime)
                    layoutDate.visibility = View.VISIBLE
                    if (noteEntity.strike) {
                        dateTime.paintFlags = dateTime.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                }
                else{
                    dateTime.text = null
                    layoutDate.visibility = View.GONE
                }

                if (!(noteEntity.location.isNullOrEmpty())) {
                    location.text = noteEntity.location
                    layoutLocation.visibility = View.VISIBLE
                }
                else{
                    location.text = null
                    layoutLocation.visibility = View.GONE
                }

                cardView.setCardBackgroundColor(noteEntity.clr)

                if(noteEntity.image != null) {
                    Glide.with(mActivity.applicationContext).load(Uri.parse(noteEntity.image)).into(img)
                    noteImageLayout.visibility = View.VISIBLE
                }
                else{
                    Glide.with(mActivity.applicationContext).clear(img)
                    noteImageLayout.visibility = View.GONE
                }

                if (noteEntity.todoList != null) {
                    val todoAdapter = HomeTodoAdapter(noteEntity.todoList as ArrayList<Todo>)
                    todoRecyclerView.setHasFixedSize(true)
                    todoRecyclerView.layoutManager = LinearLayoutManager(mActivity.applicationContext)
                    todoRecyclerView.adapter = todoAdapter
                    todoRecyclerView.visibility = View.VISIBLE
                    todoAdapter.notifyDataSetChanged()
                }
                else{
                    todoRecyclerView.visibility = View.GONE
                }
            }
        }
    }

    private fun clickItem(binding: NewNoteBinding, holder: NoteViewHolder) {
        if (holder.adapterPosition != RecyclerView.NO_POSITION) {
            task = getItem(holder.adapterPosition)
        }
        binding.apply {
            if (cardView.strokeWidth == 0) {
                cardView.strokeWidth = 8
                cardView.strokeColor = Color.parseColor("#80cbc4")
                selectList.add(task)
            } else {
                cardView.strokeWidth = 0
                cardView.strokeColor = Color.parseColor("#9e9e9e")
                selectList.remove(task)
            }
            if (selectList.size == noteList.size) {
                mItem?.icon =
                    ContextCompat.getDrawable(mActivity.applicationContext, R.drawable.ic_baseline_deselect_24)
            }
            else{
                mItem?.icon = ContextCompat.getDrawable(mActivity.applicationContext, R.drawable.ic_baseline_select_all_24)
            }
        }
        mutableLiveData.value = selectList.size.toString()
    }

    interface OnItemClickListener {
        fun onItemClick(noteEntity: NoteEntity)
        fun onDeleteClick(noteEntity: NoteEntity)
        fun onUndo(noteEntity: NoteEntity)
        fun storeActionMode(mode: ActionMode?)
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) = oldItem.Id == newItem.Id
        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) = oldItem == newItem
    }
}