package com.sandip.notefy.ui.home

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.R
import com.sandip.notefy.data.NoteEntity
import com.sandip.notefy.data.Todo

class TodoAdapter(
    context: Context,
    todoList: ArrayList<Todo>?,
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder?>() {
    private val context: Context
    private val todoList: ArrayList<Todo>?
    init {
        this.context = context
        this.todoList = todoList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        return TodoViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.add_tick_display_note,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        holder.todoTitle.text = todoList?.get(position)?.todoDescription ?: ""
        holder.todoCheckBox.isChecked = todoList?.get(position)?.completed ?: false
        if(todoList?.get(position)?.completed == true){
            holder.todoTitle.setPaintFlags(holder.todoTitle.getPaintFlags() or Paint.STRIKE_THRU_TEXT_FLAG)
        }
    }

    override fun getItemCount(): Int {
        return todoList?.size ?: -1
    }

    inner class TodoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var todoCheckBox: CheckBox
        var todoTitle: TextView

        init {
            todoCheckBox = itemView.findViewById(R.id.ch)
            todoTitle = itemView.findViewById(R.id.ti)
        }
    }


}