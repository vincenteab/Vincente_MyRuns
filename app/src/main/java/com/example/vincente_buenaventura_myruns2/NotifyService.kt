package com.example.vincente_buenaventura_myruns2

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.model.LatLng
import kotlin.math.roundToInt
import android.Manifest
import androidx.lifecycle.ViewModelProvider

class NotifyService: Service(), LocationListener {
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "MY CHANNEL ID"
    private val CHANNEL_NAME = "MY CHANNEL NAME"
    private val NOTIFY_ID = 1
    private lateinit var myBinder: MyBinder
    private var totalDistance: Double = 0.0
    private var startTime: Long = 0L
    private var lastLocation: Location? = null

    private val handlerThread = HandlerThread("LocationThread")
    private lateinit var handler: Handler
    private lateinit var locationManager: LocationManager


    private val _distanceLiveData = MutableLiveData<Double>()
    val distanceLiveData: LiveData<Double> get() = _distanceLiveData

    private val _averageSpeedLiveData = MutableLiveData<Double>()
    val averageSpeedLiveData: LiveData<Double> get() = _averageSpeedLiveData

    private val _currentSpeedLiveData = MutableLiveData<Double>()
    val currentSpeedLiveData: LiveData<Double> get() = _currentSpeedLiveData

    companion object {
        val STOP_SERVICE = "STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()

        handlerThread.start()
        handler = Handler(handlerThread.looper)
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        myBroadcastReceiver = MyBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_SERVICE)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(myBroadcastReceiver, intentFilter)
        }
        showNotification()
        initLocationManager()
        startLocationUpdatesInBackground()

        myBinder = MyBinder()
        println("debug: onCreate called")

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("debug: onStartCommand called")
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder = myBinder

    inner class MyBinder : Binder() {
        fun getService(): NotifyService = this@NotifyService
    }

    private fun showNotification() {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        val notificationIntent = Intent(this, MapDisplayActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        notificationBuilder.setContentTitle("MyRuns")
        notificationBuilder.setContentText("Recording your path now")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setSmallIcon(R.drawable.rocket)

        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)

        }

        notificationManager.notify(NOTIFY_ID, notification)

    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            unregisterReceiver(myBroadcastReceiver)
            notificationManager.cancel(NOTIFY_ID)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
        handlerThread.quitSafely()
        println("debug: Service onDestroy() called~~~")


    }


    override fun onUnbind(intent: Intent?): Boolean {
        println("debug: Service onUnBind() called~~~")
        return true
    }


    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) return

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


        if (lastLocation != null) {
            // Calculate distance between the last and current locations
            val distance = lastLocation!!.distanceTo(location) * 0.000621371 // meters to miles
            totalDistance += distance
            _distanceLiveData.postValue(totalDistance)
            println("debug: Distance between points: $distance miles")
        }

        lastLocation = location

        // Calculate elapsed time
        val elapsedTime = (System.currentTimeMillis() - startTime) / 1000f // seconds
        println("debug: Elapsed time: $elapsedTime seconds")

        if (elapsedTime > 0) {
            // Calculate average speed in m/s
            val averageSpeed = totalDistance / elapsedTime

            // Convert to km/h for logging
            val averageSpeedMph = (averageSpeed * 2.23694)

            // Log the average speed in miles per hour
            _averageSpeedLiveData.postValue(averageSpeedMph)
            println("debug: Average Speed: $averageSpeedMph mph")


        }
        val currentSpeedMph = (location.speed * 2.23694)
        _currentSpeedLiveData.postValue(currentSpeedMph)
        println("debug: Current Speed: $currentSpeedMph mph")


    }

    fun updateTrackingData(distance: Double, averageSpeed: Double, currentSpeed: Double) {
        _distanceLiveData.postValue(distance)
        _averageSpeedLiveData.postValue(averageSpeed)
        _currentSpeedLiveData.postValue(currentSpeed)
    }

    private fun startLocationUpdatesInBackground() {
        handler.post {
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        1000L, // 1-second interval
                        1f, // 1-meter minimum distance
                        this
                    )
                    startTime = System.currentTimeMillis()
                }
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }
}


