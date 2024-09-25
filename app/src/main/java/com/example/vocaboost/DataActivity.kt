package com.example.vocaboost

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vocaboost.data.database.NoteDatabase
import com.example.vocaboost.data.model.Note
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
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
    private lateinit var storageActivityResultLauncher: ActivityResultLauncher<Intent>
    private var originalNoteList: List<Note> = emptyList() // Original list of notes

    private val STORAGE_PERMISSION_CODE = 23

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_data)

        // Initialize database
        noteDatabase = NoteDatabase.getDatabase(this)

        val buttonToData = findViewById<FrameLayout>(R.id.button_to_home)
        buttonToData.setOnClickListener {
            val intent = Intent(this@DataActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        // Initialize views
        recyclerView = findViewById(R.id.recyclerView)
        textViewCount = findViewById(R.id.textViewCount)
        searchView = findViewById(R.id.searchView)
        buttonExport = findViewById(R.id.button_export)
        buttonImport = findViewById(R.id.button_import)
        buttonCreate = findViewById(R.id.fab_create)

        recyclerView.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            loadNotes() // Load notes on activity start
        }

        buttonCreate.setOnClickListener { showAddNoteDialog() }
        setupSearchView()

        // Export and import button click listeners
        buttonExport.setOnClickListener { checkPermissionsAndExport() }
        buttonImport.setOnClickListener { importDatabaseFromJson() }

        // Register storage permission result launcher
        storageActivityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult(),
            ActivityResultCallback { result ->
                if (result.resultCode == RESULT_OK) {
                    Toast.makeText(this, "Storage permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

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
        updateNoteCount(filteredNotes.size)
    }

    @SuppressLint("SetTextI18n")
    private fun updateNoteCount(count: Int) {
        textViewCount.text = "Total Notes: $count"
    }

    private suspend fun loadNotes() {
        val notes = noteDatabase.noteDao().getAllNotes().toMutableList()
        originalNoteList = notes
        itemAdapter = ItemAdapter(this, notes, noteDatabase, true) // Passing true to show icons
        recyclerView.adapter = itemAdapter
        updateNoteCount(notes.size)
    }

    private fun checkPermissionsAndExport() {
        if (checkStoragePermissions()) {
            lifecycleScope.launch { exportDatabaseAsJson("notes") }
        } else {
            requestForStoragePermissions()
        }
    }

    private fun checkStoragePermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestForStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                data = Uri.parse("package:$packageName")
            }
            storageActivityResultLauncher.launch(intent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                lifecycleScope.launch { exportDatabaseAsJson("notes") }
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun exportDatabaseAsJson(fileName: String) {
        val notes = noteDatabase.noteDao().getAllNotes()
        val json = Gson().toJson(notes)

        val exportFile = File(getExternalFilesDir(null), "$fileName-export.json")
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
        startActivityForResult(intent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                lifecycleScope.launch {
                    importDatabaseFromJsonFile(uri)
                }
            }
        }
    }

    private suspend fun importDatabaseFromJsonFile(uri: Uri) {
        val inputStream = contentResolver.openInputStream(uri)

        if (inputStream != null) {
            val notes: List<Note>
            inputStream.use { stream ->
                val type = object : TypeToken<List<Note>>() {}.type
                notes = Gson().fromJson(stream.reader(), type)
            }

            for (note in notes) {
                noteDatabase.noteDao().insert(note)
            }

            loadNotes()
            Toast.makeText(this, "Database imported successfully", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Failed to open the file. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    // Method to delete note from database
    fun deleteNoteFromDatabase(note: Note) {
        lifecycleScope.launch {
            noteDatabase.noteDao().delete(note)
            loadNotes() // Refresh the list after deletion
            Toast.makeText(this@DataActivity, "Note deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }

    fun showAddNoteDialog(note: Note? = null) {
        val addNoteDialog = AddNoteDialogFragment()
        note?.let { addNoteDialog.setNote(it) }
        addNoteDialog.listener = object : AddNoteDialogFragment.OnNoteAddedListener {
            override fun onNoteAdded(updatedNote: Note) {
                if (note == null) {
                    addNoteToDatabase(updatedNote, addNoteDialog)
                } else {
                    updateNoteInDatabase(updatedNote, addNoteDialog)
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
                loadNotes()
                Toast.makeText(this@DataActivity, "Note successfully added!", Toast.LENGTH_SHORT).show()
                addNoteDialog.dismiss()
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
}
