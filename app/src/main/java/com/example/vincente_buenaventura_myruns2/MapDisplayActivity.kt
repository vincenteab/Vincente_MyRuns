package com.example.vincente_buenaventura_myruns2

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MapDisplayActivity : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        saveButton = findViewById(R.id.btnSave)
        saveButton.setOnClickListener{
            finish()
        }

        cancelButton = findViewById(R.id.btnCancel)
        cancelButton.setOnClickListener{
            finish()
        }

    }
}