package com.sandip.notefy.ui.languages

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Language

class LanguagesAdapter(
    langList: ArrayList<Language>?,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<LanguagesAdapter.LanguagesViewHolder?>() {
    private val myList : ArrayList<Language>?
    private val itemClickListener: OnItemClickListener?
    private var selectedPosition  = 0

    init {
        myList = langList
        itemClickListener = listener
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

        val sharedPreferences =  NotefyApplication.appContext.getSharedPreferences("PREFERENCE_NAME",Context.MODE_PRIVATE)
        val pos = sharedPreferences?.getInt("position", 0)
        if (pos != null) {
            selectedPosition = pos
        }

        myList?.get(position)
            ?.let { it.isChecked?.let { it1 -> holder.flagImage.setImageResource(it1)}}
        holder.language.text = myList?.get(position)?.language


        if (selectedPosition == position) {
            holder.cardView.strokeWidth = 5
            holder.cardView.strokeColor = Color.parseColor("#80cbc4")
        }
        else{
            holder.cardView.strokeWidth = 0
            holder.cardView.strokeColor = Color.TRANSPARENT
        }

    }

    override fun getItemCount(): Int {
        return myList?.size ?: -1
    }

    inner class LanguagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var flagImage: ImageView
        var language: TextView
        var cardView : MaterialCardView

        init {
            flagImage = itemView.findViewById(R.id.flag)
            language = itemView.findViewById(R.id.language)
            cardView = itemView.findViewById(R.id.language_card_view)

            itemView.setOnClickListener {
                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                selectedPosition = adapterPosition
                notifyDataSetChanged()
                listener.onItemClick(selectedPosition)
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(flag: Int)
    }
}


