package com.sandip.notefy.ui.home


import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
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
import com.sandip.notefy.ui.home.Home.Companion.noteList


class NoteAdapter(private val listener: OnItemClickListener) :
    ListAdapter<NoteEntity, NoteAdapter.NoteViewHolder>(DiffCallback()) {
    //    var adp: TodoAdapter? = null
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
//        println("inside onbind")

        holder.bind(holder, currentItem)

    }

//    override fun getItemCount(): Int {
////        return
//    }

    inner class NoteViewHolder(private val binding: NewNoteBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {

//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    val task = getItem(position)
//                    if(!task.isHide) {
//                        noteList.add(task)
//                    }
//                }
            }
//            println("note size ${noteList.size}")
        }

        fun bind(holder: NoteViewHolder, noteEntity: NoteEntity) {
            binding.apply {
                overlay.setOnClickListener {
                    if (isEnable) {
                        // when action mode is enable
                        // call method
                        clickItem(binding, holder);
                    } else {
                        val position = adapterPosition
                        if (position != RecyclerView.NO_POSITION) {
                            val task = getItem(position)
                            listener.onItemClick(task)
                        }
                    }
                }
                overlay.setOnLongClickListener {
//                    val position = adapterPosition
//                    if (position != RecyclerView.NO_POSITION) {
//                        val task = getItem(position)
//                        listener.onItemLongClick(holder,task)
//                    }

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
                                (Home.act as LifecycleOwner?)?.let { it1 ->
                                    HomeViewModel.mutableLiveData.observe(
                                        it1,
                                        Observer<String?> { s -> // when text change
                                            // set text on action mode title
                                            if(!(s.equals("0"))){
                                                mode?.title = String.format("%s", s)
                                            }
                                            else{
                                                mode?.finish()
                                            }
                                        })
                                };
                                return false
                            }

                            override fun onActionItemClicked(
                                mode: ActionMode?,
                                item: MenuItem?
                            ): Boolean {
                                return when (item?.itemId) {

                                    R.id.select -> {
                                        item.icon = ContextCompat.getDrawable(NotefyApplication.appContext, R.drawable.ic_baseline_deselect_24);
                                        Log.d("Select all", "${selectList.size} , ${noteList.size}")

                                        if(selectList.size == noteList.size)
                                        {
                                            // when all item selected
                                            // set isselectall false
                                            isSelectAll=false
                                            // create select array list
                                            selectList.clear()
                                        }
                                        else
                                        {
                                            // when  all item unselected
                                            // set isSelectALL true
                                            isSelectAll=true;
                                            // clear select array list
                                            selectList.clear();
                                            // add value in select array list
                                            selectList.addAll(noteList)

                                        }
                                        // set text on view model
                                        HomeViewModel.mutableLiveData.value = selectList.size.toString()

                                        // notify adapter
                                        notifyDataSetChanged()
                                        println("note list size in select ${noteList.size}")

                                        true
                                    }
                                    R.id.delete -> {
                                        for (s in selectList) {
                                            listener.onDeleteClick(s)
                                            undoList.add(s)
                                            Log.d("undo1", undoList.size.toString())
                                        }
                                        Snackbar.make(itemView, "Note deleted", Snackbar.LENGTH_LONG)
                                            .setAction("UNDO") {
                                                for (s in undoList) {
                                                    listener.onUndo(s)
                                                    Log.d("undo2", undoList.size.toString())

                                                }
                                            }.show()
                                        Log.d("undo3", undoList.size.toString())

                                        mode?.finish();
                                        true
                                    }
                                    else -> false
                                }
                            }

                            override fun onDestroyActionMode(mode: ActionMode?) {
                                isEnable=false
                                isSelectAll=false
                                selectList.clear()
                                // notify adapter
                                notifyDataSetChanged()
                            }
                        }
                        Home.act.startActionMode(callback)
                    }
                    else
                    {
                        // when action mode is already enable
                        // call method
                        clickItem(binding, holder);
                    }
                    true

                    // check condition

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

    private fun clickItem(binding: NewNoteBinding, holder: NoteViewHolder) {
        val position = holder.adapterPosition
        if (position != RecyclerView.NO_POSITION) {
            task = getItem(position)
        }
        // check condition
        // check condition
        if (binding.cardView.strokeWidth == 0) {
            // when item not selected
            // visible check box image
            binding.cardView.strokeWidth = 8
            binding.cardView.strokeColor = Color.parseColor("#80cbc4")
            // set background color
            // add value in select array list
            selectList.add(task)
        } else {
            // when item selected
            // hide check box image
            binding.cardView.strokeWidth = 0
            binding.cardView.strokeColor = Color.parseColor("#9e9e9e")
            // remove value from select arrayList
            selectList.remove(task)
        }
        // set text on view model
        // set text on view model
        HomeViewModel.mutableLiveData.value = selectList.size.toString()
        Log.d("size", HomeViewModel.mutableLiveData.value.toString())
//        if(selectList.size == 0){
//            actionMode?.finish()
//        }
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