package com.sandcastle.notefy.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sandcastle.notefy.data.model.Todo
import java.io.File
import java.io.FileOutputStream
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
        fun getImageUri(context: Context?, directory: File?, fileName: String, inImage: Bitmap): Uri? {
            val imageFolder= File(directory, "images")
            var uri: Uri? = null
            try {
                imageFolder.mkdirs()
                val file = File(imageFolder, fileName)
                val outputStream = FileOutputStream(file)
                inImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                uri =
                    context?.let { FileProvider.getUriForFile(it, "com.sandcastle.notefy.provider", file) }
            } catch (e: Exception) {
                Toast.makeText(context, "" + e.message, Toast.LENGTH_LONG).show()
            }
            return uri
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