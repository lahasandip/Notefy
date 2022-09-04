package com.sandip.notefy.ui.newupdate/*
    Code written by IJApps
    github.com/IJ-Apps
    Tutorial Series: https://www.youtube.com/watch?v=9nFGR8dIu_w&list=PLLmkb5CTw5rRsR6reO-ZkbE-QJF-GstwG
*/
import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import com.sandip.notefy.R

class ListViewBAdapter(context: Context, var checks: ArrayList<Boolean>?, var list: ArrayList<String>) :
    ArrayAdapter<String?>(context, R.layout.add_tick, list as ArrayList<String?>) {
    private val obj = NewUpdateNote()

    // The method we override to provide our own layout for each View (row) in the ListView
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater =
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.add_tick, null)
            val name:CheckBox = convertView!!.findViewById<CheckBox>(R.id.check)
            val add:TextView = convertView.findViewById<TextView>(R.id.tick_de)
            val remove = convertView.findViewById<ImageView>(R.id.rmve)
            name.isChecked = checks?.get(position) ?: false
            add.text = list[position]


            // Listeners for duplicating and removing an item.
            // They use the static removeItem and addItem methods created in MainActivity.
            remove.setOnClickListener { obj.removeItem(position)
            }
            name.setOnClickListener { obj.update(position, name.isChecked)  }
//            NewUpdateNote.getPosition(position)

            // Listeners for duplicating and removing an item.
            // They use the static removeItem and addItem methods created in MainActivity.
        }
        return convertView!!
    }
}