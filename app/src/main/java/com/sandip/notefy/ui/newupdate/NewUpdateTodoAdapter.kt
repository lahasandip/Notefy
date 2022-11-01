package com.sandip.notefy.ui.newupdate

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Todo

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
            todoTitle.text = todoList?.get(position)?.todoDescription ?: ""
            todoCheckBox.isChecked = todoList?.get(position)?.completed ?: false
            if (todoList?.get(position)?.completed == true) {
                todoTitle.paintFlags =
                    todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            }
            todoCheckBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    todoTitle.paintFlags =
                        todoTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    todoList?.get(position)?.completed = true
                } else {
                    todoTitle.paintFlags =
                        todoTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    todoList?.get(position)?.completed = false
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