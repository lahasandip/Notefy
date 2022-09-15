package com.sandip.notefy.ui.help


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.R
import com.sandip.notefy.data.Help
import com.sandip.notefy.data.Language
import com.sandip.notefy.ui.languages.LanguagesViewModel


class HelpAdapter(
    helpList: ArrayList<Help>,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<HelpAdapter.HelpFeedbackViewHolder?>() {
    private val myList : ArrayList<Help>?
    private val itemClickListener: OnItemClickListener?
    var selectedPosition = -1
    private val viewModel: HelpFeedbackViewModel


    init {
        myList = helpList
        itemClickListener = listener
        viewModel = HelpFeedbackViewModel()
    }
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

        val help : Help? = myList?.get(position)

        holder.questions.text = myList?.get(position)?.question ?: ""
        holder.expandedText.text =  myList?.get(position)?.expandedText ?: ""
        if(help?.visibility == true){
            holder.expandedLayout.visibility = View.VISIBLE
            holder.arrowButton.setImageResource(R.drawable.ic_baseline_keyboard_arrow_up_24)

        }
        else{
            holder.expandedLayout.visibility = View.GONE
            holder.arrowButton.setImageResource(R.drawable.ic_outline_keyboard_arrow_down_24)


        }
        holder.itemView.setOnClickListener {
            selectedPosition = position
//            listener.onItemClick(selectedPosition)
            println("Position $selectedPosition")
            help?.visibility = !(help?.visibility)!!
            notifyItemChanged(position)

        }


//
    }

    override fun getItemCount(): Int {
        return myList?.size ?: -1
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
    interface OnItemClickListener {
        fun onItemClick(flag: Int)
    }

//    private fun fetchAccentColor(): Int {
//        val typedValue = TypedValue()
//        val theme: Theme = .theme
//        theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, typedValue, true)
//        @ColorInt val color = typedValue.data
//        return color
//    }
}

