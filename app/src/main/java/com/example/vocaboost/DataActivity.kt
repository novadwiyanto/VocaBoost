package com.example.vocaboost

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton


class DataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: List<Item>

    lateinit var button_create : FloatingActionButton

    data class Item(
        val english: String,
        val indonesian: String,
        val description: String
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data)

        // Inisialisasi RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        itemList = listOf(
            Item("Hello", "Halo", "A greeting."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Goodbye", "Selamat tinggal", "A farewell.")

        )

        itemAdapter = ItemAdapter(this, itemList)
        recyclerView.adapter = itemAdapter

        button_create = findViewById(R.id.fab_create);

        button_create.setOnClickListener {
            val addNoteDialog = AddNoteDialogFragment()

            addNoteDialog.listener = object : AddNoteDialogFragment.OnNoteAddedListener {
                override fun onNoteAdded(english: String, indonesian: String, description: String) {
                    Log.d("DataActivity", "Note added: Title = $english, Description = $description")
                }
            }

            addNoteDialog.show(supportFragmentManager, "AddNoteDialog")
        }
    }
}
