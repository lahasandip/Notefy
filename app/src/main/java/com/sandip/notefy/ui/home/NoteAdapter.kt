package com.sandip.notefy.ui.home


import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.data.entity.NoteEntity
import com.sandip.notefy.data.model.Todo
import com.sandip.notefy.databinding.NewNoteBinding

class NoteAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback()) {
//    var adp: TodoAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NewNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        binding.overlay.setOnClickListener {
//            binding.overlay.visibility = View.GONE;
//        }
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

    }

//    override fun getItemCount(): Int {
////        return
//    }

    inner class NoteViewHolder(private val binding: NewNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
//                root.setOnClickListener {
                    overlay.setOnClickListener {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val task = getItem(position)
                            listener.onItemClick(task)
                        }
                    }
//                }
//                checkBoxCompleted.setOnClickListener {
//                    val position = adapterPosition
//                    if (position != RecyclerView.NO_POSITION) {
//                        val task = getItem(position)
//                        listener.onCheckBoxClick(task, checkBoxCompleted.isChecked)
//                    }
//                }
            }

        }

        fun bind(noteEntity: NoteEntity) {
            binding.apply {
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
                if ((!(noteEntity.date.isNullOrEmpty())) &&
                    (!(noteEntity.time.isNullOrEmpty()))
                ) {
                    date.text = noteEntity.date
//                    time.text = noteEntity.time
                    layoutDate.visibility = View.VISIBLE
                }
                if (!(noteEntity.location.isNullOrEmpty())) {
                    location.text = noteEntity.location
                    layoutLocation.visibility = View.VISIBLE

                }
                cardView.setCardBackgroundColor(noteEntity.clr)

                if(noteEntity.image != null) {
//                    img.setImageURI(noteEntity.image)
                    val imageUri = Uri.parse(noteEntity.image)
                    Glide.with(NotefyApplication.appContext).load(imageUri).into(img)
                    noteImageLayout.visibility = View.VISIBLE
                }
//                adp = TodoAdapter(NotefyApplication.appContext, noteEntity.completed, noteEntity.todoDescription)
//                listview2.adapter = adp

                if (noteEntity.todoList != null) {
                    val todoAdapter = TodoAdapter(NotefyApplication.appContext,noteEntity.todoList as ArrayList<Todo>)
                    todoRecyclerView.setHasFixedSize(true)
                    todoRecyclerView.layoutManager = LinearLayoutManager(NotefyApplication.appContext)
                    todoRecyclerView.adapter = todoAdapter
                    todoRecyclerView.visibility = View.VISIBLE
                    todoAdapter.notifyDataSetChanged()
                }
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(noteEntity: NoteEntity)
        fun onAddToTrash(noteEntity: NoteEntity, isHide: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}