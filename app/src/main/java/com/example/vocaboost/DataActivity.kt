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

    private fun showAddNoteDialog() {
        val addNoteDialog = AddNoteDialogFragment()
        addNoteDialog.listener = object : AddNoteDialogFragment.OnNoteAddedListener {
            override fun onNoteAdded(english: String, indonesian: String, description: String) {
                addNoteToDatabase(english, indonesian, description, addNoteDialog) // Pass the dialog
            }
        }
        addNoteDialog.show(supportFragmentManager, "AddNoteDialog")
    }

    private fun addNoteToDatabase(english: String, indonesian: String, description: String, dialog: AddNoteDialogFragment) {
        lifecycleScope.launch {
            val exists = noteDatabase.noteDao().checkEnglishExists(english)

            if (exists > 0) {
                Toast.makeText(this@DataActivity, "Note with this English word already exists.", Toast.LENGTH_SHORT).show()
            } else {
                val newNote = Note(english = english, indonesian = indonesian, description = description)
                noteDatabase.noteDao().insert(newNote)

                val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
                itemAdapter.updateNotes(notes)

                // Close the dialog and show success toast
                dialog.dismiss() // Close the dialog
                Toast.makeText(this@DataActivity, "Note successfully added!", Toast.LENGTH_SHORT).show() // Show success toast
            }
        }
    }

    fun deleteNoteFromDatabase(note: Note, position: Int) {
        lifecycleScope.launch {
            noteDatabase.noteDao().delete(note) // Delete the note from the database
            itemAdapter.itemList.removeAt(position) // Remove the note from the adapter's list
            itemAdapter.notifyItemRemoved(position) // Notify the adapter of the removed item
        }
    }
}
