package com.example.vocaboost.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.vocaboost.data.model.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(note: Note)

    @Query("SELECT * FROM notes")
    suspend fun getAllNotes(): List<Note>

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT COUNT(*) FROM notes WHERE english = :english")
    suspend fun checkEnglishExists(english: String): Int
}
