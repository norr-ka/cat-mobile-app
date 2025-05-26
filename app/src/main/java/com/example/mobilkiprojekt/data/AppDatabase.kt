package com.example.mobilkiprojekt.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

// Definicja bazy danych Room
@Database(
    entities = [
        MediaEntity::class,
        CatEntity::class
    ],
    version = 3,
    exportSchema = false // Wyłączono eksport schematu
)
@TypeConverters(Converters::class) // Dodano globalne konwertery typów dla bazy
abstract class AppDatabase : RoomDatabase() {

    // Abstrakcyjna metoda zwracająca DAO dla mediów
    abstract fun photoDao(): MediaDao
    // Abstrakcyjna metoda zwracająca DAO dla kotów
    abstract fun catDao(): CatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null // Instancja Singleton bazy danych

        // Metoda do pobierania instancji bazy danych (Singleton)
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // Zmieniono nazwę bazy, aby objąć wszystkie dane
                )
                    // Dodano fallbackToDestructiveMigration, aby uprościć migrację
                    // W aplikacji produkcyjnej należy zaimplementować właściwą strategię migracji
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}