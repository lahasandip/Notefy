package com.sandcastle.notefy.ui.newupdate

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.model.Todo

class NewUpdateTodoAdapter(
    mTodoList: ArrayList<Todo>?,
) : RecyclerView.Adapter<NewUpdateTodoAdapter.TodoViewHolder?>() {
    private val todoList: ArrayList<Todo>? = mTodoList

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
        holder.apply {
            todoTitle.text = todoList?.get(adapterPosition)?.todoDescription ?: ""
            todoCheckBox.isChecked = todoList?.get(adapterPosition)?.completed ?: false
            if (todoList?.get(adapterPosition)?.completed == true) {
                todoTitle.paintFlags =
                    todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    todoTitle.paintFlags =
                        todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    todoList?.get(adapterPosition)?.completed = true
                } else {
                    todoTitle.paintFlags =
                        todoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    todoList?.get(adapterPosition)?.completed = false
                }
            }
            todoTitle.addTextChangedListener {
                todoList?.get(position)?.todoDescription = it.toString()
            }

            removeButton.setOnClickListener {
                todoList?.removeAt(position)
                notifyDataSetChanged()
            }
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