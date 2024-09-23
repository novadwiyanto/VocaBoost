package com.example.vocaboost

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import kotlinx.coroutines.launch
import com.example.vocaboost.data.database.NoteDatabase
import com.example.vocaboost.data.model.Note
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.reflect.TypeToken
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class DataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var buttonCreate: FloatingActionButton
    private lateinit var noteDatabase: NoteDatabase
    private lateinit var searchView: SearchView
    private lateinit var textViewCount: TextView
    private lateinit var buttonExport: Button
    private lateinit var buttonImport: Button
    private var originalNoteList: List<Note> = emptyList() // To hold the original notes

    private val REQUEST_CODE = 100 // Code for permission request

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data)

        // Initialize database
        noteDatabase = NoteDatabase.getDatabase(this)

        val buttonToHome = findViewById<Button>(R.id.button_to_home)
        buttonToHome.setOnClickListener {
            val intent = Intent(this@DataActivity, HomeActivity::class.java)
            startActivity(intent)
            finish() // Jika ingin menutup DataActivity
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        textViewCount = findViewById(R.id.textViewCount)
        searchView = findViewById(R.id.searchView)
        buttonExport = findViewById(R.id.button_export)
        buttonImport = findViewById(R.id.button_import)

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

        // Set up button listeners
        buttonExport.setOnClickListener {
            checkPermissionsAndExport()
        }
        buttonImport.setOnClickListener {
            importDatabaseFromJson()
        }
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

    @SuppressLint("SetTextI18n")
    private fun updateNoteCount(count: Int) {
        textViewCount.text = "Total Notes: $count" // Update the TextView
    }

    private suspend fun loadNotes() {
        val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
        originalNoteList = notes
        itemAdapter = ItemAdapter(this@DataActivity, notes, noteDatabase, true)
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

    fun deleteNoteFromDatabase(note: Note) {
        lifecycleScope.launch {
            noteDatabase.noteDao().delete(note) // Delete the note from the database
            loadNotes() // Load notes again to refresh the list and update the count
        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_CODE)
        } else {
            // Izin sudah diberikan, lakukan ekspor
            lifecycleScope.launch {
                exportDatabaseAsJson("notes")
            }
        }
    }

    private fun checkPermissionsAndExport() {
        checkPermissions()
    }

    private suspend fun exportDatabaseAsJson(fileName: String) {
        val notes = noteDatabase.noteDao().getAllNotes()
        val gson = Gson()
        val json = gson.toJson(notes)

        val exportFile = File(getExternalFilesDir(null), "$fileName-export.json") // Menggunakan getExternalFilesDir
        FileOutputStream(exportFile).use { outputStream ->
            outputStream.write(json.toByteArray())
        }
        Toast.makeText(this, "Database exported to ${exportFile.absolutePath}", Toast.LENGTH_SHORT).show()
    }

    private fun importDatabaseFromJson() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "application/json"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, 1) // Start the file picker
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                uri.path?.let { path ->
                    val importFile = File(path) // Convert Uri to File

                    lifecycleScope.launch {
                        importDatabaseFromJsonFile(importFile) // Call the suspend function in a coroutine
                    }
                }
            }
        }
    }

    private suspend fun importDatabaseFromJsonFile(importFile: File) {
        val gson = Gson()
        val type = object : TypeToken<List<Note>>() {}.type
        val notes: List<Note>

        FileInputStream(importFile).use { inputStream ->
            notes = gson.fromJson(inputStream.reader(), type)
        }

        // Insert notes into the database
        for (note in notes) {
            noteDatabase.noteDao().insert(note)
        }
        loadNotes() // Reload notes after import
        Toast.makeText(this, "Database imported from ${importFile.absolutePath}", Toast.LENGTH_SHORT).show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin diberikan, lakukan ekspor
                lifecycleScope.launch {
                    exportDatabaseAsJson("notes")
                }
            } else {
                Toast.makeText(this, "Permission denied. Please allow storage permission to export.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
