package com.example.vocaboost

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vocaboost.data.database.NoteDatabase
import com.example.vocaboost.data.model.Note
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var noteDatabase: NoteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        noteDatabase = NoteDatabase.getDatabase(this)

        val buttonToData = findViewById<Button>(R.id.button_to_data)
        buttonToData.setOnClickListener {
            val intent = Intent(this@HomeActivity, DataActivity::class.java)
            startActivity(intent)
            finish()
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Load initial random notes
        loadRandomNotes()

        // Set up the FloatingActionButton
        findViewById<com.google.android.material.floatingactionbutton.FloatingActionButton>(R.id.randomiseButton).setOnClickListener {
            loadRandomNotes() // Load new random notes on button click
        }
    }

    private fun loadRandomNotes() {
        lifecycleScope.launch {
            val notes = noteDatabase.noteDao().getAllNotes()
            if (notes.isNotEmpty()) {
                val randomNotes = notes.shuffled().take(10) // Ambil 10 note acak
                if (!::itemAdapter.isInitialized) {
                    itemAdapter = ItemAdapter(this@HomeActivity, randomNotes.toMutableList(), noteDatabase, false)
                    recyclerView.adapter = itemAdapter
                } else {
                    itemAdapter.updateNotes(randomNotes.toMutableList()) // Update item adapter dengan notes baru
                }
            }
        }
    }
}
