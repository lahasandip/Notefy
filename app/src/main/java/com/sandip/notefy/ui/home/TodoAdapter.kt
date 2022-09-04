package com.sandip.notefy.ui.home

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import com.sandip.notefy.R

class TodoAdapter(context: Context, var checks: ArrayList<Boolean>?, var list: ArrayList<String>?) :
    ArrayAdapter<String?>(context, R.layout.add_tick, list as ArrayList<String?>) {
    // The method we override to provide our own layout for each View (row) in the ListView
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater =
                context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.add_tick_display_note, null)
            val name:CheckBox = convertView!!.findViewById<CheckBox>(R.id.ch)
            val add:TextView = convertView.findViewById<TextView>(R.id.ti)
            name.isChecked = checks?.get(position) ?: false
            add.text = list?.get(position) ?: ""
        }
        return convertView!!
    }
}