package com.example.mobilkiprojekt.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import java.time.LocalDate

// Definicja encji dla profilu kota w bazie danych Room
@Entity(tableName = "cats")
@TypeConverters(Converters::class) // Określenie użycia konwerterów typów
data class CatEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0, // Klucz główny, generowany automatycznie
    var name: String, // Imię kota
    var breed: String, // Rasa kota
    var birthDate: LocalDate, // Data urodzenia (konwertowana przez TypeConverter)
    var weight: Double, // Waga kota
    var allergies: String, // Alergie (jako tekst)
    var medicalHistory: List<MedicalEventEntity>, // Historia medyczna (lista konwertowana przez TypeConverter)
    var notes: String // Dodatkowe notatki
)

// Definicja encji dla zdarzenia medycznego (używana w liście medicalHistory)
// Nie jest to osobna tabela, ale struktura danych używana w TypeConverterze
data class MedicalEventEntity(
    val id: String, // Unikalny identyfikator zdarzenia
    val type: String, // Typ zdarzenia (np. "WETERYNARZ", "SZCZEPIENIE")
    val date: LocalDate, // Data zdarzenia (konwertowana przez TypeConverter w głównym konwerterze listy)
    val description: String // Opis zdarzenia
)