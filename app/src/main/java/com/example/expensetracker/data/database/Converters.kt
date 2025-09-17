package com.example.expensetracker.data.database

import androidx.room.TypeConverter
import com.example.expensetracker.model.Category

class Converters {
    @TypeConverter
    fun categoryToString(cat: Category?): String? = cat?.name

    @TypeConverter
    fun stringToCategory(raw: String?): Category? =
        raw?.let { Category.fromLabelOrNull(it) }
}
