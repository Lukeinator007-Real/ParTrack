package com.partrack.app.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromIntList(value: List<Int>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toIntList(value: String): List<Int> {
        val type = object : TypeToken<List<Int>>() {}.type
        return gson.fromJson(value, type)
    }

    @TypeConverter
    fun fromScoreMap(value: Map<String, Map<Int, Int>>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toScoreMap(value: String): Map<String, Map<Int, Int>> {
        val type = object : TypeToken<Map<String, Map<Int, Int>>>() {}.type
        return gson.fromJson(value, type)
    }
}
