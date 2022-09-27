package com.sandip.notefy.ui.languages


import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.sandip.notefy.NotefyApplication
import com.sandip.notefy.R
import com.sandip.notefy.data.model.Language
import kotlin.properties.Delegates


class LanguagesAdapter(
    langList: ArrayList<Language>?,
    private val listener: OnItemClickListener
) :
    RecyclerView.Adapter<LanguagesAdapter.LanguagesViewHolder?>() {
    private val myList : ArrayList<Language>?
    private val itemClickListener: OnItemClickListener?
    private var selectedPosition  = -1

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
//        val color = fetchAccentColor()
//        println("My $color")
//        if(selectedPosition == position){
//            holder.cardView.setBackgroundColor(Color.parseColor("#9575cd"))
//                    }
//        else{
//
//        }
//        val sharedPreferences2 =  NotefyApplication.appContext.getSharedPreferences("PREFERENCE",Context.MODE_PRIVATE)
//        val pos = sharedPreferences2?.getInt("pos", 0)
//        if (pos != null) {
//            selectedPosition = pos
//        }


        myList?.get(position)
            ?.let { it.isChecked?.let { it1 -> holder.flagImage.setImageResource(it1) } }
        holder.language.text = myList?.get(position)?.language



//        holder.itemView.setOnClickListener {
//            if(position==RecyclerView.NO_POSITION) return@setOnClickListener
//            selectedPosition = position
//            notifyDataSetChanged()
//            listener.onItemClick(selectedPosition)
//
//        }
//        holder.itemView.setOnClickListener {
//            selectedPosition = position
//            notifyItemChanged(selectedPosition)

//            listener.onItemClick(selectedPosition)

//            println("Position $selectedPosition")
//            holder.isChecked.visibility = View.VISIBLE
//            holder.isChecked.isSelected = true
//            notifyItemChanged(selectedPosition)
//            holder.cardView.setBackgroundColor(if (selectedPosition === position) Color.GREEN else Color.TRANSPARENT)
//
//
//        }
        if (selectedPosition == position) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#80CBC4"))
            Log.d("Lang", "Background changed to color")
        }
        else {
            Log.d("Lang", "Background changed trans")

        }
        Log.d("Lang", "Inside func2 $selectedPosition ,  $position")

    }
//

    override fun getItemCount(): Int {
        return myList?.size ?: -1
    }

    inner class LanguagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var flagImage: ImageView
        var language: TextView
        var cardView : CardView
//        var isChecked : FloatingActionButton

        init {
            flagImage = itemView.findViewById(R.id.flag)
            language = itemView.findViewById(R.id.language)
            cardView = itemView.findViewById(R.id.language_cardview)
//            isChecked = itemView.findViewById(R.id.card_checked)



            itemView.setOnClickListener {


                if (adapterPosition == RecyclerView.NO_POSITION) return@setOnClickListener
                selectedPosition = adapterPosition
                notifyDataSetChanged()
                listener.onItemClick(selectedPosition)
                Log.d("Lang", "Inside func $selectedPosition,  $adapterPosition")

            }

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

