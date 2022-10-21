package com.sandip.notefy.ui.newupdate

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Todo
import com.sandip.notefy.ui.newupdate.NewUpdateNote.Companion.todoAdapter

class NewUpdateTodoAdapter(
    context: Context?,
    todoList: ArrayList<Todo>?,
) : RecyclerView.Adapter<NewUpdateTodoAdapter.TodoViewHolder?>() {
    private val context: Context?
    private val todoList: ArrayList<Todo>?
    private val recyclerView = NewUpdateNote.recyclerView
    init {
        this.context = context
        this.todoList = todoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.add_tick,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.todoTitle.text = todoList?.get(position)?.todoDescription ?: ""
        holder.todoCheckBox.isChecked = todoList?.get(position)?.completed ?: false
        if(todoList?.get(position)?.completed == true) {
            holder.todoTitle.paintFlags = holder.todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }
        holder.todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                holder.todoTitle.paintFlags = holder.todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                todoList?.get(position)?.completed = true
            } else {
                holder.todoTitle.paintFlags = holder.todoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                todoList?.get(position)?.completed = false
            }
        }
        holder.todoTitle.addTextChangedListener {
            todoList?.get(position)?.todoDescription = it.toString()
        }

        holder.removeButton.setOnClickListener {
            todoList?.removeAt(position)
            recyclerView?.adapter = todoAdapter
            todoAdapter?.notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return todoList?.size ?: -1
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var todoCheckBox: CheckBox
        var todoTitle: TextView
        var removeButton: ImageView

        init {
            todoCheckBox = itemView.findViewById(R.id.check)
            todoTitle = itemView.findViewById(R.id.tick_de)
            removeButton = itemView.findViewById(R.id.remove)
        }
    }
}