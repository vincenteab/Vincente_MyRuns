package com.example.vincente_buenaventura_myruns1

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity


class ManualInputActivity : AppCompatActivity() {
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_manual)

        saveButton = findViewById(R.id.button27)
        cancelButton = findViewById(R.id.button28)

        saveButton.setOnClickListener{
            finish()
        }

        cancelButton.setOnClickListener{
            finish()
        }

    }


}