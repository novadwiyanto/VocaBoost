package com.example.vocaboost

import android.os.Bundle
import android.widget.SearchView
import android.widget.TextView
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
    private lateinit var searchView: SearchView
    private lateinit var textViewCount: TextView
    private var originalNoteList: List<Note> = emptyList() // To hold the original notes

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data)

        // Initialize database
        noteDatabase = NoteDatabase.getDatabase(this)

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        textViewCount = findViewById(R.id.textViewCount) // Initialize TextView for note count
        searchView = findViewById(R.id.searchView)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load data from the database
        lifecycleScope.launch {
            loadNotes()
        }

        // Initialize Floating Action Button
        buttonCreate = findViewById(R.id.fab_create)
        buttonCreate.setOnClickListener { showAddNoteDialog() }

        setupSearchView()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterNotes(newText)
                return true
            }
        })
    }

    private fun filterNotes(query: String?) {
        val filteredNotes = originalNoteList.filter { note ->
            note.english.contains(query ?: "", ignoreCase = true) ||
                    note.indonesian.contains(query ?: "", ignoreCase = true)
        }
        itemAdapter.updateNotes(filteredNotes.toMutableList())
        updateNoteCount(filteredNotes.size) // Update count after filtering
    }

    private fun updateNoteCount(count: Int) {
        textViewCount.text = "Total Notes: $count" // Update the TextView
    }

    private suspend fun loadNotes() {
        val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
        originalNoteList = notes
        itemAdapter = ItemAdapter(this@DataActivity, notes, noteDatabase)
        recyclerView.adapter = itemAdapter
        updateNoteCount(notes.size) // Update count after loading
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
                loadNotes() // Reload notes to update original list and count
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
                loadNotes() // Reload notes to update original list and count
                Toast.makeText(this@DataActivity, "Note successfully updated!", Toast.LENGTH_SHORT).show()
                addNoteDialog.dismiss() // Dismiss the passed dialog
            }
        }
    }

    private fun refreshNotes() {
        lifecycleScope.launch {
            loadNotes() // Reload notes to update original list and count
        }
    }

    fun deleteNoteFromDatabase(note: Note, position: Int) {
        lifecycleScope.launch {
            noteDatabase.noteDao().delete(note) // Delete the note from the database
            // Load notes again to refresh the list and update the count
            loadNotes()
        }
    }

    private fun onNoteClicked(note: Note) {
        showAddNoteDialog(note) // Show dialog with existing note for updating
    }
}
