package com.sandcastle.notefy.ui.home

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.model.Todo

class HomeTodoAdapter( mTodoList: ArrayList<Todo>?)
    : RecyclerView.Adapter<HomeTodoAdapter.TodoViewHolder?>() {

    private val todoList: ArrayList<Todo>? = mTodoList

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
        holder.apply {
            todoTitle.text = todoList?.get(position)?.todoDescription ?: ""
            todoCheckBox.isChecked = todoList?.get(position)?.completed ?: false
            if (todoList?.get(position)?.completed == true) {
                todoTitle.paintFlags =
                    todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
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