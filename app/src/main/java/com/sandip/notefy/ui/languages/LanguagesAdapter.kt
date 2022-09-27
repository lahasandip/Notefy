package com.sandip.notefy.ui.languages


import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Language


class LanguagesAdapter(
    laguageList: ArrayList<Language>?,
    private val listener: LanguagesAdapter.OnItemClickListener
) :
    RecyclerView.Adapter<LanguagesAdapter.LanguagesViewHolder?>() {
    private val myList : ArrayList<Language>?
    private val itemClickListener: OnItemClickListener?
    var selectedPosition = -1
    private val viewModel: LanguagesViewModel


    init {
        myList = laguageList
        itemClickListener = listener
        viewModel = LanguagesViewModel()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LanguagesViewHolder {
        return LanguagesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.language_card,
                parent,
                false
            )
        )
    }


    override fun onBindViewHolder(holder: LanguagesViewHolder, position: Int) {
//        val color = fetchAccentColor()
//        println("My $color")
//        if(selectedPosition == position){
//            holder.cardView.setBackgroundColor(Color.parseColor("#9575cd"))
//                    }
//        else{
//
//        }

        myList?.get(position)?.let { it.flag?.let { it1 -> holder.flagImage.setImageResource(it1) } }
        holder.language.text = myList?.get(position)?.language
        holder.itemView.setOnClickListener {
            selectedPosition = position
            listener.onItemClick(selectedPosition)
            println("Position $selectedPosition")
            holder.isChecked.visibility = View.VISIBLE

        }


//
    }

    override fun getItemCount(): Int {
        return myList?.size ?: -1
    }

    inner class LanguagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var flagImage: ImageView
        var language: TextView
        var cardView : CardView
        var isChecked : FloatingActionButton

        init {
            flagImage = itemView.findViewById(R.id.flag)
            language = itemView.findViewById(R.id.language)
            cardView = itemView.findViewById(R.id.language_cardview)
            isChecked = itemView.findViewById(R.id.card_checked)



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

