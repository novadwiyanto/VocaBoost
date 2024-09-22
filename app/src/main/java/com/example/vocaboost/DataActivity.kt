package com.example.vocaboost

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class DataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: List<Item>

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
    }
}
