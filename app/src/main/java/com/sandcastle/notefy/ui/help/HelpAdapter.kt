package com.sandcastle.notefy.ui.help

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sandcastle.notefy.R
import com.sandcastle.notefy.data.model.Help

class HelpAdapter(helpList: ArrayList<Help>) : RecyclerView.Adapter<HelpAdapter.HelpFeedbackViewHolder?>() {
    private val myList = helpList
    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelpFeedbackViewHolder {
        return HelpFeedbackViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.help_card,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: HelpFeedbackViewHolder, position: Int) {

        val help = myList[position]
        holder.apply {
            questions.text = myList[position].question ?: ""
            expandedText.text = myList[position].expandedText ?: ""
            if (help.visibility == true) {
                expandedLayout.visibility = View.VISIBLE
                arrowButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)
            } else {
                expandedLayout.visibility = View.GONE
                arrowButton.setImageResource(R.drawable.ic_outline_keyboard_arrow_down_24)
            }
            itemView.setOnClickListener {
                selectedPosition = adapterPosition
                help.visibility = !(help.visibility)!!
                notifyItemChanged(position)
            }
        }
    }
    override fun getItemCount(): Int {
        return myList.size
    }

    inner class HelpFeedbackViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var questions: TextView
        var arrowButton: ImageView
        var expandedText : TextView
        var expandedLayout : ConstraintLayout
        init {
            questions = itemView.findViewById(R.id.questions)
            arrowButton = itemView.findViewById(R.id.arrow_button)
            expandedText = itemView.findViewById(R.id.expanded_text)
            expandedLayout = itemView.findViewById(R.id.layout_expanded_text)
        }
    }
}

