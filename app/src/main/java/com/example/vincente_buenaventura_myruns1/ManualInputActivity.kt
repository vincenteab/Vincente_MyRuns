package com.example.vincente_buenaventura_myruns1

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.Calendar
import kotlin.time.Duration


class ManualInputActivity : AppCompatActivity(),
    TimePickerDialog.OnTimeSetListener, DatePickerDialog.OnDateSetListener{
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var dateButton: Button
    private lateinit var timeButton: Button
    private lateinit var durationButton: Button
    private lateinit var distanceButton: Button
    private lateinit var caloriesButton: Button
    private lateinit var heartRateButton: Button
    private lateinit var commentButton: Button
    private val calendar = Calendar.getInstance()
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

        dateButton = findViewById(R.id.button7)

        dateButton.setOnClickListener{
            val datePickerDialog = DatePickerDialog(
                this, this, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        timeButton = findViewById(R.id.button21)
        timeButton.setOnClickListener{
            val timePickerDialog = TimePickerDialog(
                this, this,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE), true
            )
            timePickerDialog.show()
        }

        durationButton = findViewById(R.id.button22)
        durationButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.duration_dialog, null)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }


            val dialog = builder.create()


            dialog.show()
        }

        distanceButton = findViewById(R.id.button23)
        distanceButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.distance_dialog, null)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }
            val dialog = builder.create()


            dialog.show()
        }

        caloriesButton = findViewById(R.id.button24)
        caloriesButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.calories_dialog, null)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }
            val dialog = builder.create()


            dialog.show()
        }

        heartRateButton = findViewById(R.id.button25)
        heartRateButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.heart_rate_dialog, null)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }
            val dialog = builder.create()


            dialog.show()
        }

        commentButton = findViewById(R.id.button26)
        commentButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.comment_manual_dialog, null)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }
            val dialog = builder.create()


            dialog.show()
        }

    }



    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
    }

    override fun onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) {
    }
}