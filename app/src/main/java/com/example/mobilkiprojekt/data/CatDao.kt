package com.example.mobilkiprojekt.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Interfejs dostępu do danych (DAO) dla encji CatEntity
@Dao
interface CatDao {

    // Wstawia nowego kota do bazy. Jeśli kot już istnieje (konflikt klucza), zastępuje go.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cat: CatEntity)

    // Aktualizuje istniejącego kota w bazie danych.
    @Update
    suspend fun update(cat: CatEntity)

    // Usuwa kota z bazy danych.
    @Delete
    suspend fun delete(cat: CatEntity)

    // Pobiera wszystkie koty z bazy danych i zwraca je jako Flow.
    // Flow automatycznie emituje nową listę, gdy dane w tabeli się zmienią.
    @Query("SELECT * FROM cats ORDER BY name ASC")
    fun getAllCats(): Flow<List<CatEntity>>

    // Pobiera jednego kota na podstawie jego ID.
    @Query("SELECT * FROM cats WHERE id = :id")
    suspend fun getCatById(id: Long): CatEntity?
}