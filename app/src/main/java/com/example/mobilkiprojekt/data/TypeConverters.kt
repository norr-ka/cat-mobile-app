package com.example.mobilkiprojekt.data

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate

// Klasa zawierająca konwertery typów dla bazy danych Room
class Converters {

    // Konwerter dla typu LocalDate na Long (przechowuje liczbę dni od epoki)
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDate? {
        return value?.let { LocalDate.ofEpochDay(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: LocalDate?): Long? {
        return date?.toEpochDay()
    }

    // Konwerter dla listy MedicalEventEntity na String (JSON) i odwrotnie
    private val gson = Gson() // Instancja Gson do serializacji/deserializacji

    @TypeConverter
    fun fromMedicalEventList(value: List<MedicalEventEntity>?): String? {
        // Serializacja listy do formatu JSON
        return value?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toMedicalEventList(value: String?): List<MedicalEventEntity>? {
        // Deserializacja z formatu JSON do listy obiektów
        return value?.let {
            val listType = object : TypeToken<List<MedicalEventEntity>>() {}.type
            gson.fromJson(it, listType)
        }
    }
}