package com.example.data

import androidx.room.TypeConverter

class KhataTypeConverters {
    @TypeConverter
    fun fromEntryType(type: EntryType): String = type.code

    @TypeConverter
    fun toEntryType(code: String): EntryType = EntryType.fromCode(code)
}
