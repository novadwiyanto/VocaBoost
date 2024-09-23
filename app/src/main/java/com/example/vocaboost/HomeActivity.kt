package com.example.vocaboost

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val buttonToData = findViewById<Button>(R.id.button_to_data)
        buttonToData.setOnClickListener {
            val intent = Intent(this@HomeActivity, DataActivity::class.java)
            startActivity(intent)
            finish() // Jika ingin menutup DataActivity
        }

    }
}