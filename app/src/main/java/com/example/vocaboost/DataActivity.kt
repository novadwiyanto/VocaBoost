package com.example.vocaboost

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch
import com.example.vocaboost.data.database.NoteDatabase
import com.example.vocaboost.data.model.Note

class DataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var buttonCreate: FloatingActionButton
    private lateinit var noteDatabase: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data)

        // Initialize database
        noteDatabase = NoteDatabase.getDatabase(this)

        // Setup RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load data from the database
        lifecycleScope.launch {
            val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
            itemAdapter = ItemAdapter(this@DataActivity, notes, noteDatabase)
            recyclerView.adapter = itemAdapter
        }

        // Initialize Floating Action Button
        buttonCreate = findViewById(R.id.fab_create)
        buttonCreate.setOnClickListener { showAddNoteDialog() }
    }

    fun showAddNoteDialog(note: Note? = null) {
        val addNoteDialog = AddNoteDialogFragment()
        note?.let { addNoteDialog.setNote(it) } // Set note if updating
        addNoteDialog.listener = object : AddNoteDialogFragment.OnNoteAddedListener {
            override fun onNoteAdded(updatedNote: Note) {
                if (note == null) {
                    addNoteToDatabase(updatedNote, addNoteDialog) // Pass dialog reference
                } else {
                    updateNoteInDatabase(updatedNote, addNoteDialog) // Pass dialog reference
                }
            }
        }
        addNoteDialog.show(supportFragmentManager, "AddNoteDialog")
    }

    private fun addNoteToDatabase(note: Note, addNoteDialog: AddNoteDialogFragment) {
        lifecycleScope.launch {
            val exists = noteDatabase.noteDao().checkEnglishExists(note.english)

            if (exists > 0) {
                Toast.makeText(this@DataActivity, "Note with this English word already exists.", Toast.LENGTH_SHORT).show()
            } else {
                noteDatabase.noteDao().insert(note)
                refreshNotes()
                Toast.makeText(this@DataActivity, "Note successfully added!", Toast.LENGTH_SHORT).show()
                addNoteDialog.dismiss() // Dismiss the passed dialog
            }
        }
    }

    private fun updateNoteInDatabase(note: Note, addNoteDialog: AddNoteDialogFragment) {
        lifecycleScope.launch {
            val exists = noteDatabase.noteDao().checkOtherEnglishExists(note.english, note.id)

            if (exists > 0) {
                Toast.makeText(this@DataActivity, "Note with this English word already exists.", Toast.LENGTH_SHORT).show()
            } else {
                noteDatabase.noteDao().insert(note) // Use insert with REPLACE strategy to update
                refreshNotes()
                Toast.makeText(this@DataActivity, "Note successfully updated!", Toast.LENGTH_SHORT).show()
                addNoteDialog.dismiss() // Dismiss the passed dialog
            }
        }
    }

    private fun refreshNotes() {
        lifecycleScope.launch {
            val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
            itemAdapter.updateNotes(notes)
        }
    }

    fun deleteNoteFromDatabase(note: Note, position: Int) {
        lifecycleScope.launch {
            noteDatabase.noteDao().delete(note) // Delete the note from the database
            itemAdapter.removeNoteAt(position) // Remove the note from the adapter's list
        }
    }

    private fun onNoteClicked(note: Note) {
        showAddNoteDialog(note) // Show dialog with existing note for updating
    }
}