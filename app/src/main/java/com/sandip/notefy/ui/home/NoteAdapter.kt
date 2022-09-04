package com.sandip.notefy.ui.home


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.data.TodoEntity
import com.sandip.notefy.databinding.NoteBinding
import com.sandip.notefy.ui.newupdate.ListViewBAdapter
import com.sandip.notefy.ui.newupdate.NewUpdateNote
import com.sandip.notefy.ui.newupdate.NewUpdateNote.Companion.completed
import javax.inject.Inject

class NoteAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback()) {
    var adp: TodoAdapter? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = NoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)

    }

//    override fun getItemCount(): Int {
////        return
//    }

    inner class NoteViewHolder(private val binding: NoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        listener.onItemClick(task)
                    }
                }
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
//                checkBoxCompleted.isChecked = task.completed
//                textViewName.text = task.name
//                textViewName.paint.isStrikeThruText = task.completed
//                labelPriority.isVisible = task.important
                titlecard.text = noteEntity.title
                body.text = noteEntity.body
                if (!(noteEntity.url.isNullOrEmpty())) {
                    urlLink.text = noteEntity.url
                    urlLink.visibility = View.VISIBLE
                }
                if ((!(noteEntity.date.isNullOrEmpty())) &&
                    (!(noteEntity.time.isNullOrEmpty()))
                ) {
                    date.text = noteEntity.date
                    time.text = noteEntity.time
                    reminder1.visibility = View.VISIBLE
                }
                if (!(noteEntity.location.isNullOrEmpty())) {
                    place.text = noteEntity.location
                    location1.visibility = View.VISIBLE

                }
                clr.setBackgroundColor(noteEntity.clr)

//                if(noteEntity.image != null) {
//                    img22.setImageBitmap(noteEntity.image)
//                    imgFrame.visibility = View.VISIBLE
//                }
                adp = TodoAdapter(NotefyApplication.appContext, noteEntity.completed, noteEntity.todoDescription)
                listview2.adapter = adp

            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(noteEntity: NoteEntity)
//        fun onCheckBoxClick(noteEntity: NoteEntity, isChecked: Boolean)
    }

    class DiffCallback : DiffUtil.ItemCallback<NoteEntity>() {
        override fun areItemsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: NoteEntity, newItem: NoteEntity) =
            oldItem == newItem
    }
}