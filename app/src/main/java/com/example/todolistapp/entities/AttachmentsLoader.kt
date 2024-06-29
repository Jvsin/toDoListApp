package com.example.todolistapp.entities

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class AttachmentsLoader {

    @TypeConverter
    public fun fromAttachmentsList(attachments: MutableList<String>): String {
        return Gson().toJson(attachments)
    }

    @TypeConverter
    public fun toAttachmentsList(attachmentsJson: String): MutableList<String> {
        val type = object : TypeToken<MutableList<String>>() {}.type
        return Gson().fromJson(attachmentsJson, type)
    }


}