package com.example.vocaboost

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: ItemAdapter
    private lateinit var itemList: List<Item>
    private lateinit var button_create: FloatingActionButton

    // Item data class implementing Parcelable
    data class Item(
        val english: String,
        val indonesian: String,
        val description: String
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(english)
            parcel.writeString(indonesian)
            parcel.writeString(description)
        }

        override fun describeContents(): Int {
            return 0
        }

        companion object CREATOR : Parcelable.Creator<Item> {
            override fun createFromParcel(parcel: Parcel): Item {
                return Item(parcel)
            }

            override fun newArray(size: Int): Array<Item?> {
                return arrayOfNulls(size)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Populate the item list
        itemList = listOf(
            Item("Hello", "Halo", "A greeting."),
            Item("Thank you", "Terima kasih", "An expression of gratitude."),
            Item("Goodbye", "Selamat tinggal", "A farewell.")
            // Add more items as needed
        )

        // Set the adapter
        itemAdapter = ItemAdapter(this, itemList)
        recyclerView.adapter = itemAdapter

        // Initialize the Floating Action Button
        button_create = findViewById(R.id.fab_create)

        button_create.setOnClickListener {
            val addNoteDialog = AddNoteDialogFragment()

            addNoteDialog.listener = object : AddNoteDialogFragment.OnNoteAddedListener {
                override fun onNoteAdded(english: String, indonesian: String, description: String) {
                    Log.d("DataActivity", "Note added: Title = $english, Description = $description")
                    // Here you can add the new note to the list and update the adapter if needed
                }
            }

            addNoteDialog.show(supportFragmentManager, "AddNoteDialog")
        }
    }
}
