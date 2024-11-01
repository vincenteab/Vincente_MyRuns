package com.example.vincente_buenaventura_myruns2

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TimePicker
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar


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
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var factory: HistoryViewModelFactory
    private lateinit var repo: HistoryRepo
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var database: HistoryDatabase

    private val calendar = Calendar.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_input_manual)
        var inputType = intent.getIntExtra("inputType", 0)
        var activityType = intent.getIntExtra("activityType", 0)
        val calendar = Calendar.getInstance()
        var date = ""+(calendar.get(Calendar.MONTH)+1)+" "+calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(Calendar.YEAR)
        var time = ""+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(Calendar.SECOND)
        var duration = 0.0
        var distance = 0.0
        var calories = 0.0
        var heartRate = 0.0
        var comment = ""
        var units = ""
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
        val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)

        if (savedRadioButtonId == -1 || savedRadioButtonId == 2131231126){
            units = "miles"
        }else if (savedRadioButtonId == 2131231125){
            units = "kilometers"
        }


        database = HistoryDatabase.getInstance(this)
        databaseDao = database.historyDatabaseDao
        repo = HistoryRepo(databaseDao)
        factory = HistoryViewModelFactory(repo)
        historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)


        saveButton = findViewById(R.id.button27)
        cancelButton = findViewById(R.id.button28)
        saveButton.setOnClickListener{

            val entry = HistoryEntry()
            entry.inputType = inputType
            entry.activityType = activityType
            entry.dateTime = time + " " + date
            entry.duration = duration
            entry.distance = distance
            entry.calories = calories
            entry.heartRate = heartRate
            entry.comment = comment
            entry.units = units



            historyViewModel.insert(entry)

            finish()
        }
        cancelButton.setOnClickListener{
            finish()
        }

        dateButton = findViewById(R.id.button7)

        dateButton.setOnClickListener{
            val datePickerDialog = DatePickerDialog(
                this, {_, selectedYear, selectedMonth, selectedDay ->
                    date = "" + (selectedMonth +1) + " " + selectedDay + " " + selectedYear
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

            datePickerDialog.show()


        }

        timeButton = findViewById(R.id.button21)
        timeButton.setOnClickListener{
            val timePickerDialog = TimePickerDialog(
                this, {_, selectedHour, selectedMinute ->
                    time = "" + selectedHour + ":" + selectedMinute+":00"
                }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE),true)

            timePickerDialog.show()


        }

        durationButton = findViewById(R.id.button22)
        durationButton.setOnClickListener{
            val infl = LayoutInflater.from(this)
            val dialogView = infl.inflate(R.layout.duration_dialog, null)

            val editText: EditText = dialogView.findViewById(R.id.editTextNumber)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                duration = editText.text.toString().toDouble()
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

            val editText: EditText = dialogView.findViewById(R.id.editTextNumber2)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                distance = editText.text.toString().toDouble()
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

            val editText: EditText = dialogView.findViewById(R.id.editTextNumber3)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                calories = editText.text.toString().toDouble()
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

            val editText: EditText = dialogView.findViewById(R.id.editTextNumber4)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                heartRate = editText.text.toString().toDouble()
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

            val editText: EditText = dialogView.findViewById(R.id.editTextText2)

            val builder = AlertDialog.Builder(this)
            builder.setView(dialogView)

            builder.setPositiveButton("OK") { dialog, _ ->
                comment = editText.text.toString()
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