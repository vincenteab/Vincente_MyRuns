package com.example.vincente_buenaventura_myruns2

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import java.util.Calendar
import kotlin.math.roundToInt


class MapDisplayActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var markerOptions: MarkerOptions
    private var mapCentered = false
    private lateinit var polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private var currentLocationMarker: Marker? = null
    private var startMarker: Marker? = null
    private lateinit var trackingViewModel: MapViewModel
    private lateinit var activityText : TextView
    private lateinit var avgSpeedText : TextView
    private lateinit var currSpeedText : TextView
    private lateinit var distanceText : TextView
    private lateinit var caloriesText : TextView
    private lateinit var deleteTextView: TextView
    private var locationList: ArrayList<LatLng> = ArrayList()

    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var repo: HistoryRepo
    private lateinit var factory: HistoryViewModelFactory
    private var activityArray = mutableListOf<String>()




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent.getBooleanExtra("display", true)){
            setContentView(R.layout.activity_display_map_entry)

            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)

        }else{
            setContentView(R.layout.activity_display_map)
            val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
            val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)
            var savedUnits = ""
            if (savedRadioButtonId == -1 || savedRadioButtonId == 2131231128){
                savedUnits = "miles"
            }else if (savedRadioButtonId == 2131231127){
                savedUnits = "kilometers"
            }

            println("debug: radiobutton: $savedRadioButtonId mapdisplayactivity saved units: $savedUnits")

            activityText = findViewById(R.id.typeTextView)
            avgSpeedText = findViewById(R.id.avgSpeedTextView)
            currSpeedText = findViewById(R.id.curSpeedTextView)
            distanceText = findViewById(R.id.distanceTextView)
            caloriesText = findViewById(R.id.calorieTextView)
            trackingViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
            trackingViewModel.bindService(this)

            var activityType = 0
            locationList = ArrayList()
            if (intent.getIntExtra("inputType", 0) == 2){

                trackingViewModel.activityType.observe(this) { activity ->
                    activityText.text = "Type: $activity"
                    activityArray.add(activity)
                }



            }else if (intent.getIntExtra("inputType", 0) == 1){
                activityType = intent.getIntExtra("activityType", 0)
                when (activityType) {
                    0 -> activityText.text = "Type: Running"
                    1 -> activityText.text = "Type: Walking"
                    2 -> activityText.text = "Type: Standing"
                    3 -> activityText.text = "Type: Cycling"
                    4 -> activityText.text = "Type: Hiking"
                    5 -> activityText.text = "Type: Downhill Skiing"
                    6 -> activityText.text = "Type: Cross-Country Skiing"
                    7 -> activityText.text = "Type: Snowboarding"
                    8 -> activityText.text = "Type: Skating"
                    9 -> activityText.text = "Type: Swimming"
                    10 -> activityText.text = "Type: Mountain Biking"
                    11 -> activityText.text = "Type: Wheelchair"
                    12 -> activityText.text = "Type: Elliptical"
                    13 -> activityText.text = "Type: Other"
                }
            }






            saveButton = findViewById(R.id.saveButton)
            cancelButton = findViewById(R.id.cancelButton)
            var avgerageSpeed = 0.0
            var distanceRecorded = 0.0
            var caloriesRecorded = 0.0
            var time = 0.0





            trackingViewModel.distance.observe(this) { distance ->
                val temp = String.format("%.2f", distance)
                if (savedUnits == "miles"){
                    distanceText.text = "Distance: $temp miles"
                }else{
                    distanceText.text = "Distance: $temp km"
                }

                distanceRecorded = distance

            }
            trackingViewModel.averageSpeed.observe(this) { avgSpeed ->
                val temp2 = String.format("%.2f", avgSpeed)
                if (savedUnits == "miles"){
                    avgSpeedText.text = "Average Speed: $temp2 mph"
                }else{
                    avgSpeedText.text = "Average Speed: $temp2 kmph"
                }

                avgerageSpeed = avgSpeed
            }

            trackingViewModel.currentSpeed.observe(this) { currentSpeed ->
                val temp3 = String.format("%.2f", currentSpeed)
                if (savedUnits == "miles"){
                    currSpeedText.text = "Current Speed: $temp3 mph"
                }else{
                    currSpeedText.text = "Current Speed: $temp3 kmph"
                }
            }

            trackingViewModel.caloriesBurned.observe(this) { calories ->
                val temp4 = calories.toInt()
                caloriesText.text = "Calories: $temp4"
                caloriesRecorded = calories.toDouble()
            }

            trackingViewModel.timeElapsed.observe(this) { timeElapsed ->
                time = timeElapsed
                println("debug: Time: $time")
            }

            trackingViewModel.locationList.observe(this) { currentLocation ->
                locationList.add(currentLocation)



                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentLocation, 17f)
                mMap.animateCamera(cameraUpdate)

                if (!mapCentered){
                    startMarker = mMap.addMarker(
                        MarkerOptions().position(currentLocation).title("Start")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                    )

                    mapCentered = true
                }

                polylineOptions.add(currentLocation)
                polylines.add(mMap.addPolyline(polylineOptions))

                currentLocationMarker?.remove()

                currentLocationMarker = mMap.addMarker(markerOptions.position(currentLocation).title("Current Location").icon(
                    BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))


            }




            cancelButton.setOnClickListener{
                val intent2 = Intent()
                intent2.action = NotifyService.STOP_SERVICE
                sendBroadcast(intent2)
                finish()
            }

            saveButton.setOnClickListener{
                val byteArray = convertLatLngListToByteArray(locationList)
                database = HistoryDatabase.getInstance(this)
                databaseDao = database.historyDatabaseDao
                repo = HistoryRepo(databaseDao)
                factory = HistoryViewModelFactory(repo)
                historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

                val entry = HistoryEntry()
                if (intent.getIntExtra("inputType", 0) == 2){
                    entry.inputType = 2
                    val activityTemp = findMostCommonString(activityArray)
                    if (activityTemp == "Running"){
                        entry.activityType = 0
                    }else if (activityTemp == "Walking"){
                        entry.activityType = 1
                    }else if (activityTemp == "Standing"){
                        entry.activityType = 2
                    }else if (activityTemp == "Other"){
                        entry.activityType = 13
                    }
                }else {
                    entry.inputType = 1
                    entry.activityType = activityType
                }


                entry.distance = distanceRecorded
                entry.calories = caloriesRecorded
                entry.duration = time
                val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
                val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)
                var units = ""
                if (savedRadioButtonId == -1 || savedRadioButtonId == 2131231128){
                    units = "miles"
                }else if (savedRadioButtonId == 2131231127){
                    units = "kilometers"
                }
                entry.units = units
                val calendar = Calendar.getInstance()
                val date = ""+(calendar.get(Calendar.MONTH)+1)+" "+calendar.get(Calendar.DAY_OF_MONTH)+" "+calendar.get(
                    Calendar.YEAR)
                val time = ""+calendar.get(Calendar.HOUR_OF_DAY)+":"+calendar.get(Calendar.MINUTE)+":"+calendar.get(
                    Calendar.SECOND)
                entry.dateTime = time + " " + date
                entry.avgSpeed = avgerageSpeed
                entry.coordinates = byteArray
                historyViewModel.insert(entry)

                val intent2 = Intent()
                intent2.action = NotifyService.STOP_SERVICE
                sendBroadcast(intent2)
                finish()
            }

            val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            mapFragment.getMapAsync(this)
        }





    }


    fun findMostCommonString(array: MutableList<String>): String? {
        return array.groupingBy { it }
            .eachCount()
            .maxByOrNull { it.value }?.key
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        if (intent.getBooleanExtra("display", true)){
            val byteArray = intent.getByteArrayExtra("coordinates")
            val latLngList = byteArray?.let { convertByteArrayToLatLngList(it) }
            val activityType = intent.getIntExtra("activityType", 0)
            val distance = intent.getDoubleExtra("distance", 0.0)
            val avgSpeed = intent.getDoubleExtra("avgSpeed", 0.0)
            val calories = intent.getDoubleExtra("calories", 0.0)
            val units = intent.getStringExtra("units")
            val id = intent.getLongExtra("id", 0)
            val inputType = intent.getIntExtra("inputType", 0)

            activityText = findViewById(R.id.typeTextView)
            avgSpeedText = findViewById(R.id.avgSpeedTextView)
            currSpeedText = findViewById(R.id.curSpeedTextView)
            distanceText = findViewById(R.id.distanceTextView)
            caloriesText = findViewById(R.id.calorieTextView)
            deleteTextView = findViewById(R.id.deleteButton)

            when (activityType) {
                0 -> activityText.text = "Type: Running"
                1 -> activityText.text = "Type: Walking"
                2 -> activityText.text = "Type: Standing"
                3 -> activityText.text = "Type: Cycling"
                4 -> activityText.text = "Type: Hiking"
                5 -> activityText.text = "Type: Downhill Skiing"
                6 -> activityText.text = "Type: Cross-Country Skiing"
                7 -> activityText.text = "Type: Snowboarding"
                8 -> activityText.text = "Type: Skating"
                9 -> activityText.text = "Type: Swimming"
                10 -> activityText.text = "Type: Mountain Biking"
                11 -> activityText.text = "Type: Wheelchair"
                12 -> activityText.text = "Type: Elliptical"
                13 -> activityText.text = "Type: Other"
            }


            val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
            val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)

            val temp = String.format("%.2f", avgSpeed)

            if (savedRadioButtonId == -1){
                avgSpeedText.text = "Average Speed: $temp mph"
            }else if (savedRadioButtonId == 2131231127 && units == "miles"){
                avgSpeedText.text = "Average Speed: "+(((avgSpeed/ 0.621371)* 100).roundToInt() / 100.0).toString()+" kmph"
            }else if (savedRadioButtonId == 2131231128 && units == "kilometers"){
                avgSpeedText.text = "Average Speed: "+(((avgSpeed* 0.621371)* 100).roundToInt() / 100.0).toString()+" mph"
            }else{
                if (units == "miles"){
                    avgSpeedText.text = "Average Speed: $temp mph"
                }   else{
                    avgSpeedText.text = "Average Speed: $temp kmph"
                }
            }




            currSpeedText.text = "Current Speed: n/a"

            val temp2 = calories.toInt()
            caloriesText.text = "Calories: $temp2"

            val temp3 = String.format("%.2f", distance)
            if (savedRadioButtonId == -1){
                distanceText.text = "Distance: $temp3 miles"
            }else if (savedRadioButtonId == 2131231127 && units == "miles"){
                distanceText.text = "Distance: "+(((distance/ 0.621371)* 100).roundToInt() / 100.0).toString()+" km"
            }else if (savedRadioButtonId == 2131231128 && units == "kilometers"){
                distanceText.text = "Distance: "+(((distance* 0.621371)* 100).roundToInt() / 100.0).toString()+" miles"
            }else{
                if (units == "miles"){
                    distanceText.text = "Distance: $temp3 miles"
                }   else{
                    distanceText.text = "Distance: $temp3 km"
                }
            }



            if (latLngList != null) {
                if (latLngList.isNotEmpty()) {
                    // Place the starting marker
                    val startPoint = latLngList.first()
                    mMap.addMarker(
                        MarkerOptions()
                            .position(startPoint)
                            .title("Start")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)) // Green marker
                    )


                    // Place the ending marker
                    val endPoint = latLngList.last()
                    mMap.addMarker(
                        MarkerOptions()
                            .position(endPoint)
                            .title("End")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)) // Red marker
                    )

                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(endPoint, 17f)
                    mMap.animateCamera(cameraUpdate)
                }
            }

            polylineOptions.color(Color.BLACK)

            if (latLngList != null) {
                polylineOptions.addAll(latLngList)
            }

            polylines.add(mMap.addPolyline(polylineOptions))

            deleteTextView.setOnClickListener{
                database = HistoryDatabase.getInstance(this)
                databaseDao = database.historyDatabaseDao
                repo = HistoryRepo(databaseDao)
                factory = HistoryViewModelFactory(repo)
                historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

                historyViewModel.delete(id)
                finish()
            }
        }
    }







    override fun onDestroy() {
        super.onDestroy()
        if (::trackingViewModel.isInitialized){
            trackingViewModel.unbindService(this)
        }

    }


    fun convertLatLngListToByteArray(latLngList: ArrayList<LatLng>): ByteArray {
        val gson = Gson()
        val jsonString = gson.toJson(latLngList) // Convert to JSON string
        return jsonString.toByteArray(Charsets.UTF_8) // Convert to byte array
    }

    fun convertByteArrayToLatLngList(byteArray: ByteArray): ArrayList<LatLng> {
        val gson = Gson()
        val jsonString = byteArray.toString(Charsets.UTF_8) // Convert byte array to JSON string
        val latLngType = object : com.google.gson.reflect.TypeToken<ArrayList<LatLng>>() {}.type
        return gson.fromJson(jsonString, latLngType) // Convert JSON string to ArrayList<LatLng>
    }

    }
