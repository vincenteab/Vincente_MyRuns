package com.example.vincente_buenaventura_myruns2

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import kotlin.math.roundToInt

class DisplayEntryActivity : AppCompatActivity() {
    private var id: Long = 0
    private lateinit var inputType: TextView
    private lateinit var activityType: TextView
    private lateinit var dateTime: TextView
    private lateinit var duration: TextView
    private lateinit var distance: TextView
    private lateinit var calories: TextView
    private lateinit var heartRate: TextView
    private lateinit var delete: Button

    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var factory: HistoryViewModelFactory
    private lateinit var repo: HistoryRepo
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var database: HistoryDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_display_entry)
        inputType = findViewById(R.id.textView20)
        activityType = findViewById(R.id.textView33)
        dateTime = findViewById(R.id.textView32)
        duration = findViewById(R.id.textView31)
        distance = findViewById(R.id.textView30)
        calories = findViewById(R.id.textView29)
        heartRate = findViewById(R.id.textView28)

        delete = findViewById(R.id.button9)

        id = intent.getLongExtra("id",0)


        database = HistoryDatabase.getInstance(this)
        databaseDao = database.historyDatabaseDao
        repo = HistoryRepo(databaseDao)
        factory = HistoryViewModelFactory(repo)
        historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)



        if (intent.getIntExtra("inputType",0) == 0 ) {
            inputType.text = "Manual Entry"
        }

        when (intent.getIntExtra("activityType",0)) {
            0 -> activityType.text = "Running"
            1 -> activityType.text = "Cycling"
            2 -> activityType.text = "Walking"
            3 -> activityType.text = "Hiking"
            4 -> activityType.text = "Downhill Skiing"
            5 -> activityType.text = "Cross-Country Skiing"
            6 -> activityType.text = "Snowboarding"
            7 -> activityType.text = "Skating"
            8 -> activityType.text = "Swimming"
            9 -> activityType.text = "Mountain Biking"
            10 -> activityType.text = "Wheelchair"
            11 -> activityType.text = "Elliptical"
            12 -> activityType.text = "Other"
        }


        var tempDate = intent.getStringExtra("dateTime")

        var tempMonth = convertMonth(tempDate)

        dateTime.text = tempMonth

        val durationVal = intent.getDoubleExtra("duration", 0.0)

        val minutes = kotlin.math.floor(durationVal).toInt()
        val temp = durationVal - minutes
        val seconds = (temp * 60).toInt()


        duration.text = minutes.toString()+" mins "+seconds.toString()+" secs"

        var units = ""

        val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
        val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)
        val distanceVal = intent.getDoubleExtra("distance", 0.0)
        val distanceUnits = intent.getStringExtra("units")
        if (savedRadioButtonId == -1){
            units = distanceVal.toString()+" miles"
        }else if (savedRadioButtonId == 2131231125 && distanceUnits == "miles"){
           units = (((distanceVal/ 0.621371)* 100).roundToInt() / 100.0).toString()+" km"
        }else if (savedRadioButtonId == 2131231126 && distanceUnits == "kilometers"){
            units = (((distanceVal* 0.621371)* 100).roundToInt() / 100.0).toString()+" miles"
        }else{
            if (distanceUnits == "miles"){
                units = ((distanceVal* 100).roundToInt() / 100.0).toString()+" miles"
        }   else{
                units = ((distanceVal* 100).roundToInt() / 100.0).toString()+" km"
            }
        }



        distance.text = units

        calories.text = intent.getDoubleExtra("calories",0.0).toString()+" cals"

        heartRate.text = intent.getDoubleExtra("heartRate",0.0).toString()+" bpm"

        delete.setOnClickListener{
            //delete the entry from the database
            historyViewModel.delete(id)
            finish()
        }
    }

    fun convertMonth(month: String?): String{
        var temp = month?.split(" ")?.toMutableList()

        var temp2 = temp?.get(1)

        var tempMonth = ""
        when(temp2){
            "1" -> tempMonth = "Jan"
            "2" -> tempMonth = "Feb"
            "3" -> tempMonth = "Mar"
            "4" -> tempMonth = "Apr"
            "5" -> tempMonth = "May"
            "6" -> tempMonth = "Jun"
            "7" -> tempMonth = "Jul"
            "8" -> tempMonth = "Aug"
            "9" -> tempMonth = "Sep"
            "10" -> tempMonth = "Oct"
            "11" -> tempMonth = "Nov"
            "12" -> tempMonth = "Dec"
        }
        temp?.set(1, tempMonth)

        if (temp != null) {

            return temp.joinToString(" ")
        }
        return ""


    }
}