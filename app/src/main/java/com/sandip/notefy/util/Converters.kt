package com.sandip.notefy.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sandip.notefy.data.model.Todo


class Converters {
    companion object{
        var gson = Gson()
    }

    @TypeConverter
    fun stringToTODOList(data: String?): List<Todo>? {
        if (data == null) {
            return emptyList<Todo>()
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