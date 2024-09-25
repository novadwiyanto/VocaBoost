package com.example.vocaboost.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import androidx.room.OnConflictStrategy
import com.example.vocaboost.data.model.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: Note)

    @Update
    suspend fun update(note: Note)

    @Query("SELECT * FROM notes ORDER BY indonesian ASC, id DESC")
    suspend fun getAllNotes(): List<Note>

    @Delete
    suspend fun delete(note: Note)

    @Query("SELECT COUNT(*) FROM notes WHERE LOWER(english) = LOWER(:english)")
    suspend fun checkEnglishExists(english: String): Int

    @Query("SELECT COUNT(*) FROM notes WHERE LOWER(english) = LOWER(:english) AND id != :noteId")
    suspend fun checkOtherEnglishExists(english: String, noteId: Long): Int

}
