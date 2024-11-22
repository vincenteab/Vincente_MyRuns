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
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.AsyncTask
import android.os.SystemClock
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import java.util.LinkedList
import java.util.Queue
import kotlin.math.max
import weka.core.Attribute
import weka.core.DenseInstance
import weka.core.Instance
import weka.core.Instances
import weka.core.converters.ArffSaver
import weka.core.converters.ConverterUtils
import java.util.concurrent.ArrayBlockingQueue

class NotifyService: Service(), LocationListener, SensorEventListener {
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver
    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "MY CHANNEL ID"
    private val CHANNEL_NAME = "MY CHANNEL NAME"
    private val NOTIFY_ID = 1
    private lateinit var myBinder: MyBinder
    private var totalDistance: Double = 0.0
    private var startTime: Long = 0L
    private var lastLocation: Location? = null
    private var totalCalories: Double = 0.0
    private var userWeightKg: Double = 70.0 // Default weight, update based on user input
    private var activityMET: Double = 3.5 // Default MET for walking, adjust based on speed
    private var averageSpeed: Double = 0.0
    private var lifeTime: Double = 0.0
    private var savedUnits = ""

    private lateinit var sensorManager: SensorManager
    private val handlerThread = HandlerThread("LocationThread")

    private lateinit var handler: Handler

    private lateinit var locationManager: LocationManager

    private val _caloriesLiveData = MutableLiveData<Double>()
    val caloriesLiveData: LiveData<Double> get() = _caloriesLiveData

    private val _distanceLiveData = MutableLiveData<Double>()
    val distanceLiveData: LiveData<Double> get() = _distanceLiveData

    private val _averageSpeedLiveData = MutableLiveData<Double>()
    val averageSpeedLiveData: LiveData<Double> get() = _averageSpeedLiveData

    private val _currentSpeedLiveData = MutableLiveData<Double>()
    val currentSpeedLiveData: LiveData<Double> get() = _currentSpeedLiveData

    private val _locationListLiveData = MutableLiveData<LatLng>()
    val locationListLiveData: LiveData<LatLng> get() = _locationListLiveData

    private val _timeElapsedLiveData = MutableLiveData<Double>()
    val timeElapsedLiveData: LiveData<Double> get() = _timeElapsedLiveData

    private val _activityTypeLiveData = MutableLiveData<String>()
    val activityTypeLiveData: LiveData<String> get() = _activityTypeLiveData

    private lateinit var mAsyncTask: OnSensorChangedTask
    private lateinit var mAccelerometer: Sensor

    private val blockSize = 64
    private var mAccBuffer = ArrayBlockingQueue<Double>(blockSize)
    private var counter = 0

    private val periodicHandler = Handler()
    private val periodicUpdateRunnable = object : Runnable {
        override fun run() {
            updateAverageSpeed()
            periodicHandler.postDelayed(this, 1000) // Update every second
        }
    }




    companion object {
        val STOP_SERVICE = "STOP_SERVICE"
    }

    override fun onCreate() {
        super.onCreate()
        mAccBuffer = ArrayBlockingQueue(2048)
        val sharedPreferences: SharedPreferences = this.getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
        val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)

        if (savedRadioButtonId == -1 || savedRadioButtonId == 2131231133){
            savedUnits = "miles"
        }else if (savedRadioButtonId == 2131231132){
            savedUnits = "kilometers"
        }

        println("debug: radioButton: $savedRadioButtonId notifyservice Saved Units: $savedUnits")
        lifeTime = SystemClock.elapsedRealtime().toDouble()
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
        periodicHandler.post(periodicUpdateRunnable)
        myBinder = MyBinder()

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        mAsyncTask = OnSensorChangedTask()
        mAsyncTask.execute()

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
        totalDistance = 0.0
        startTime = 0L
        totalCalories = 0.0
        lastLocation = null
        averageSpeed = 0.0
        lifeTime = 0.0
        mAsyncTask.cancel(true)
        periodicHandler.removeCallbacks(periodicUpdateRunnable)

        sensorManager.unregisterListener(this)


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
            e.printStackTrace()
        }
    }

    override fun onLocationChanged(location: Location) {
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        var currentSpeedMph: Double = 0.0


        if (lastLocation != null) {
            // Calculate distance between the last and current locations
            if (savedUnits == "kilometers") {
                val distance = lastLocation!!.distanceTo(location) * 0.001 // meters to kilometers
                totalDistance += distance
                println("debug: Distance between points: $distance kilometers")
            } else {
                val distance = lastLocation!!.distanceTo(location) * 0.000621371 // meters to miles
                totalDistance += distance
                println("debug: Distance between points: $distance miles")
            }


            val elapsedTimeHours = (System.currentTimeMillis() - startTime) / (3600 * 1000.0) // Time in hours
            println("debug: Elapsed Time: $elapsedTimeHours hours")

            if (elapsedTimeHours > 0) {
                // Update MET based on activity type or speed
                if (averageSpeed > 0) {
                    activityMET = 3.5 + 0.5 * averageSpeed
                }

                // Calculate calories burned
                val caloriesBurned = activityMET * userWeightKg * elapsedTimeHours * 3.5 / 200
                totalCalories += caloriesBurned
                _caloriesLiveData.postValue(totalCalories)
                println("debug: Calories Burned: $totalCalories")
            }



        }else{
            println("debug: Last location is null")
        }

        lastLocation = location

        if (savedUnits == "kilometers") {
            currentSpeedMph = (location.speed * 3.6)
        }else{
            currentSpeedMph = (location.speed * 2.23694)
        }


        updateTrackingData(distance = totalDistance,
            currentSpeed =  currentSpeedMph,
            location = latLng)

    }

    fun updateTrackingData(distance: Double, currentSpeed: Double, location: LatLng) {
        println("debug: distance: $distance, currentSpeed: $currentSpeed, location: $location")
        _distanceLiveData.postValue(distance)
        _currentSpeedLiveData.postValue(currentSpeed)
        _locationListLiveData.postValue(location)
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

    private fun updateAverageSpeed() {
        val elapsedTimeHours = (System.currentTimeMillis() - startTime) / 1000.0 / 60.0 / 60.0// Time in minutes


        val elapsedTime = (SystemClock.elapsedRealtime() - lifeTime) /1000.0 / 60.0


        _timeElapsedLiveData.postValue(elapsedTime)


        if (elapsedTimeHours > 0) {
            val averageSpeedMph = totalDistance / elapsedTimeHours // Average speed in mph
            _averageSpeedLiveData.postValue(averageSpeedMph)
            averageSpeed = averageSpeedMph
            if (savedUnits == "kilometers") {
                println("debug: Updated Average Speed: $averageSpeedMph km/h")
            }else{
                println("debug: Updated Average Speed: $averageSpeedMph mph")
            }
        }
    }





    inner class OnSensorChangedTask: AsyncTask<Void, Void, Void>(){
        override fun doInBackground(vararg params: Void?): Void? {
            val fft = FFT(blockSize)
            val accBlock = DoubleArray(blockSize)
            val im = DoubleArray(blockSize)
            var counter =0
            var max = Double.MIN_VALUE
            while (true){
                try {
                    if (isCancelled()){
                        return null
                    }
                    accBlock[counter++] = mAccBuffer.take().toDouble()
                    if (counter == blockSize){
                        counter = 0
                        max = accBlock.maxOrNull() ?: 0.0
                        fft.fft(accBlock, im)
                        val featureVector = Array<Any?>(blockSize + 1) { 0.0 }
                        for (i in accBlock.indices){
                            val mag = Math.sqrt(accBlock[i] * accBlock[i] + im[i] * im[i])
                            featureVector[i] = mag
                            im[i] = 0.0
                        }
                        featureVector[blockSize] = max

                        val predictedLabel = try {
                            WekaClassifier.classify(featureVector)
                        }catch (e: Exception){
                            e.printStackTrace()
                            Double.NaN
                        }
                        var temp = ""
                        when (predictedLabel){
                            0.0 -> temp = "Standing"
                            1.0 -> temp = "Walking"
                            2.0 -> temp = "Running"
                            3.0 -> temp = "Other"
                        }
                        _activityTypeLiveData.postValue(temp)
                    }
                }catch (e: InterruptedException){
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val m = Math.sqrt((event.values[0] * event.values[0] + event.values[1] * event.values[1] + (event.values[2]
                    * event.values[2])).toDouble())

            // Inserts the specified element into this queue if it is possible
            // to do so immediately without violating capacity restrictions,
            // returning true upon success and throwing an IllegalStateException
            // if no space is currently available. When using a
            // capacity-restricted queue, it is generally preferable to use
            // offer.
            try {
                mAccBuffer.add(m)
            } catch (e: IllegalStateException) {

                // Exception happens when reach the capacity.
                // Doubling the buffer. ListBlockingQueue has no such issue,
                // But generally has worse performance
                val newBuf = ArrayBlockingQueue<Double>(mAccBuffer.size * 2)
                mAccBuffer.drainTo(newBuf)
                mAccBuffer = newBuf
                mAccBuffer.add(m)
            }
        }
    }



    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Do nothing
    }


}


