package com.sandip.notefy.util

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sandip.notefy.data.model.Todo


class Converters {
    companion object{
        var gson = Gson()
    }

//    @TypeConverter
//    fun fromBitmap(bitmap: Bitmap?): ByteArray {
//        val outputStream = ByteArrayOutputStream()
//        bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
//        return outputStream.toByteArray()
//    }
//
//    @TypeConverter
//    fun toBitmap(byteArray: ByteArray?): Bitmap? {
//        return byteArray?.size?.let { BitmapFactory.decodeByteArray(byteArray, 0, it) }
//    }


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