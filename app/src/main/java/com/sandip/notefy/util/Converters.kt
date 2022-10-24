package com.sandip.notefy.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sandip.notefy.data.model.Todo
import java.text.SimpleDateFormat
import java.util.*


class Converters {
    companion object {
        var gson = Gson()

        fun getDateFormat(noteDateTime: String): String {
            var simpleDateFormat = SimpleDateFormat("yyyy-MM-dd-h:m", Locale.getDefault())
            val newDate = simpleDateFormat.parse(noteDateTime)
            simpleDateFormat = SimpleDateFormat("MMM d, ''yy, h:m", Locale.getDefault())
            return newDate?.let { it1 -> simpleDateFormat.format(it1) }.toString()
        }
    }
    @TypeConverter
    fun stringToTODOList(data: String?): List<Todo>? {
        if (data == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<Todo?>?>() {}.type
        return gson.fromJson<List<Todo>>(
            data,
            listType
        )
    }

    @TypeConverter
    fun todoListToString(someObjects: List<Todo?>?): String? {
        return gson.toJson(someObjects)
    }
}