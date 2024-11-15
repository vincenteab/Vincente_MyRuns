package com.example.vincente_buenaventura_myruns2

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.maps.CameraUpdate
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

class MapDisplayActivity : AppCompatActivity(), LocationListener, OnMapReadyCallback {
    private lateinit var locationManager: LocationManager
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
    private var notifyService: NotifyService? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_map)

        activityText = findViewById(R.id.typeTextView)
        avgSpeedText = findViewById(R.id.avgSpeedTextView)
        currSpeedText = findViewById(R.id.curSpeedTextView)
        distanceText = findViewById(R.id.distanceTextView)

        val activityType = intent.getIntExtra("activityType", 0)
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

//        val intent3 = Intent(this, NotifyService::class.java)
//        bindService(intent3, trackingViewModel, 0)


        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)

        trackingViewModel = ViewModelProvider(this).get(MapViewModel::class.java)
        trackingViewModel.bindService(this)

        trackingViewModel.distance.observe(this) { distance ->
            val temp = String.format("%.2f", distance)
            println("debug: distance observed: $distance")
            distanceText.text = "Distance: $temp miles"


        }
        trackingViewModel.averageSpeed.observe(this) { avgSpeed ->
            val temp2 = String.format("%.2f", avgSpeed)
            println("debug: Average Speed observed: $avgSpeed mph")
            avgSpeedText.text = "Average Speed: $temp2 mph"
        }

        trackingViewModel.currentSpeed.observe(this) { currentSpeed ->
            val temp3 = String.format("%.2f", currentSpeed)
            println("debug: Current Speed observed: $currentSpeed mph")
            currSpeedText.text = "Current Speed: $temp3 mph"
        }




        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        cancelButton.setOnClickListener{
            val intent2 = Intent()
            intent2.action = NotifyService.STOP_SERVICE
            sendBroadcast(intent2)
            finish()
        }



    }



    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        markerOptions = MarkerOptions()
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()

        checkPermission()
    }

    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)

        } catch (e: SecurityException) {
        }
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
        mMap.animateCamera(cameraUpdate)

        if (!mapCentered){
            startMarker = mMap.addMarker(
                MarkerOptions().position(latLng).title("Start")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            )

            mapCentered = true
        }


        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))

        currentLocationMarker?.remove()

        currentLocationMarker = mMap.addMarker(markerOptions.position(latLng).title("Current Location").icon(
            BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)))

    }


    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
        trackingViewModel.unbindService(this)
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) ActivityCompat.requestPermissions(this, arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION), 0) else initLocationManager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }
}