Vincente Buenaventura
301422086

**I WANT TO NOTE THAT I AM USING MY 24-HOUR GRACE PERIOD ON THIS MYRUNS4 ASSIGNMENT**


I have used, implemented, and built on top of the code below, that was shown in lectures, in my assignment 4.



package com.example.i_am_here_map_kotlin

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions


class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener,
    GoogleMap.OnMapClickListener, GoogleMap.OnMapLongClickListener{
    private lateinit var mMap: GoogleMap

    private val PERMISSION_REQUEST_CODE = 0
    private lateinit var locationManager: LocationManager

    private var mapCentered = false
    private lateinit var  markerOptions: MarkerOptions
    private lateinit var  polylineOptions: PolylineOptions
    private lateinit var  polylines: ArrayList<Polyline>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map)
                as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        mMap.setOnMapClickListener(this)
        mMap.setOnMapLongClickListener(this)
        polylineOptions = PolylineOptions()
        polylineOptions.color(Color.BLACK)
        polylines = ArrayList()
        markerOptions = MarkerOptions()

        checkPermission()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (locationManager != null)
            locationManager.removeUpdates(this)
    }

    fun initLocationManager() {
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
        println("debug: onlocationchanged() ${location.latitude} ${location.longitude}")
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        if (!mapCentered) {
            val cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 17f)
            mMap.animateCamera(cameraUpdate)
            markerOptions.position(latLng)
            mMap.addMarker(markerOptions)
            polylineOptions.add(latLng)
            mapCentered = true
        }
    }

    override fun onMapClick(latLng: LatLng) {
        for (i in polylines.indices) polylines[i].remove()
        polylineOptions.points.clear()
    }

    override fun onMapLongClick(latLng: LatLng) {
        markerOptions.position(latLng!!)
        mMap.addMarker(markerOptions)
        polylineOptions.add(latLng)
        polylines.add(mMap.addPolyline(polylineOptions))
    }

    fun checkPermission() {
        if (Build.VERSION.SDK_INT < 23) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), PERMISSION_REQUEST_CODE)
        else
            initLocationManager()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) initLocationManager()
        }
    }
}


package com.example.notifydemokotlin

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class NotifyService : Service() {
    private val web = "https://www.sfu.ca/computing.html"
    private val PENDINGINTENT_REQUEST_CODE = 0
    private val NOTIFY_ID = 11
    private val CHANNEL_ID = "notification channel"
    private lateinit var myBroadcastReceiver: MyBroadcastReceiver
    private lateinit var notificationManager: NotificationManager

    companion object{
        val STOP_SERVICE_ACTION = "stop service action"
    }

    override fun onCreate() {
        super.onCreate()
        println("debug: onCreate called")
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()
        myBroadcastReceiver = MyBroadcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_SERVICE_ACTION)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(myBroadcastReceiver, intentFilter, RECEIVER_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(myBroadcastReceiver, intentFilter)
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        println("debug: onStartCommand called")
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        println("debug: onDestroy called")
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    fun showNotification() {
        val webpageIntent = Intent(Intent.ACTION_VIEW, Uri.parse(web))
        val pendingIntent = PendingIntent.getActivity(
            this, PENDINGINTENT_REQUEST_CODE,
            webpageIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
        notificationBuilder.setContentTitle("title")
        notificationBuilder.setContentText("content")
        notificationBuilder.setContentIntent(pendingIntent)
        notificationBuilder.setSmallIcon(R.drawable.rocket)
        notificationBuilder.setAutoCancel(true)
        val notification = notificationBuilder.build()

        if (Build.VERSION.SDK_INT > 26) {
            val notificationChannel = NotificationChannel(CHANNEL_ID,
                "channel name", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFY_ID, notification)
    }

    inner class MyBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            stopSelf()
            notificationManager.cancel(NOTIFY_ID)
            unregisterReceiver(myBroadcastReceiver)
        }
    }
}



class MainActivity : AppCompatActivity() {
    private lateinit var startButton: Button
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startservice)
        stopButton = findViewById(R.id.stopservice)

        startButton.setOnClickListener(){
            val intent = Intent(this, NotifyService::class.java)
            startService(intent)
        }

        stopButton.setOnClickListener(){
            val intent = Intent()
            intent.action = NotifyService.STOP_SERVICE_ACTION
            sendBroadcast(intent)
        }
    }
}



package com.example.binddemokotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.util.*

class CounterService : Service(){
    private lateinit var notificationManager: NotificationManager
    private val NOTIFICATION_ID = 777
    private val CHANNEL_ID = "notification channel"

    private lateinit var  myBinder: MyBinder
    private var msgHandler: Handler? = null
    companion object{
        val INT_KEY = "int key"
        val MSG_INT_VALUE = 0
    }
    /////////
    private var counter = 0
    private lateinit var myTask: MyTask
    private lateinit var timer: Timer

    override fun onCreate() {
        super.onCreate()
        Log.d("debug", "Service onCreate() called")
        myTask = MyTask()
        timer = Timer()
        timer.schedule(myTask, 0, 1000L)
        showNotification()
        myBinder = MyBinder()
//        msgHandler = null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("debug: Service onStartCommand() called everytime startService() is called; startId: $startId flags: $flags")
        return START_NOT_STICKY
    }

    //XD:Multiple clients can connect to the service at once. However, the system calls your
    // service's onBind() method to retrieve the IBinder only when the first client binds.
    // The system then delivers the same IBinder to any additional clients that bind, without
    // calling onBind() again.
    override fun onBind(intent: Intent?): IBinder? {
        println("debug: Service onBind() called")
        return myBinder
    }

    inner class MyBinder : Binder() {
        fun setmsgHandler(msgHandler: Handler) {
            this@CounterService.msgHandler = msgHandler
        }
    }

    //XD: return false will allow you to unbind only once. Play with it.
    //XD: Return true if you would like to have the service's onRebind(Intent) method later called
    // when new clients bind to it.
    override fun onUnbind(intent: Intent?): Boolean {
        println("debug: Service onUnBind() called~~~")
        msgHandler = null
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        println("debug: Service onDestroy")
        cleanupTasks()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        println("debug: app removed from the application list")
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks(){
        notificationManager.cancel(NOTIFICATION_ID)
        if (timer != null)
            timer.cancel()
        counter = 0
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        ) //XD: see book p1019 why we do not use Notification.Builder
        notificationBuilder.setSmallIcon(R.drawable.rocket)
        notificationBuilder.setContentTitle("Service has started")
        notificationBuilder.setContentText("Tap me to go back")
        notificationBuilder.setContentIntent(pendingIntent)
        val notification = notificationBuilder.build()
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                "channel name",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    inner class MyTask : TimerTask() {
        override fun run() {
            try {
                counter += 1
                println("xd: counter: $counter")

                if(msgHandler != null){
                    val bundle = Bundle()
                    bundle.putInt(INT_KEY, counter)
                    val message = msgHandler!!.obtainMessage()
                    message.data = bundle
                    message.what = MSG_INT_VALUE
                    msgHandler!!.sendMessage(message)
                }
            } catch (t: Throwable) { // you should always ultimately catch all // exceptions in timer tasks.
                println("debug: Timer Tick Failed. $t")
            }
        }
    }
}


package com.example.binddemokotlin

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.Message
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel : ViewModel(), ServiceConnection {
    private var myMessageHandler: MyMessageHandler
    private val _counter = MutableLiveData<Int>()
    val counter: LiveData<Int>
        get() {
            return _counter
        }

    init {
        myMessageHandler = MyMessageHandler(Looper.getMainLooper())
    }

    override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
        println("debug: ViewModel: onServiceConnected() called; ComponentName: $name")
        val tempBinder = iBinder as CounterService.MyBinder
        tempBinder.setmsgHandler(myMessageHandler)
    }

    override fun onServiceDisconnected(name: ComponentName) {
        println("debug: Activity: onServiceDisconnected() called~~~")
    }

    inner class MyMessageHandler(looper: Looper) : Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == CounterService.MSG_INT_VALUE) {
                val bundle = msg.data
                _counter.value = bundle.getInt(CounterService.INT_KEY)
            }
        }
    }

}


class MainUIFragment : Fragment() {
    private lateinit var startServiceButton: Button
    private lateinit var stopServiceButton: Button
    private lateinit var bindButton: Button
    private lateinit var unbindButton: Button
    private lateinit var intValueLabel: TextView

    private lateinit var appContext: Context
    private var isBind = false
    private lateinit var myViewModel: MyViewModel
    private val BIND_STATUS_KEY = "bind_status_key"
    private lateinit var backPressedCallback: OnBackPressedCallback
    private lateinit var intent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent = Intent(activity, CounterService::class.java)
        appContext = requireActivity().applicationContext
        myViewModel = ViewModelProvider(requireActivity()).get(MyViewModel::class.java)
        myViewModel.counter.observe(this, Observer { it ->
            intValueLabel.text = "Int Message: $it"
        })
        if(savedInstanceState != null)
            isBind = savedInstanceState.getBoolean(BIND_STATUS_KEY)
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_main_u_i, container, false)
        startServiceButton = view.findViewById(R.id.btnStart)
        stopServiceButton = view.findViewById(R.id.btnStop)
        bindButton = view.findViewById(R.id.btnBind)
        unbindButton = view.findViewById(R.id.btnUnbind)
        intValueLabel = view.findViewById(R.id.textIntValue)
        startServiceButton.setOnClickListener(){ it -> onClick(it)}
        stopServiceButton.setOnClickListener(){ it -> onClick(it)}
        bindButton.setOnClickListener(){ it -> onClick(it)}
        unbindButton.setOnClickListener(){ it -> onClick(it)}

        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                println("debug: back button pressed")
                unBindService()
                requireActivity().stopService(intent)
                isEnabled = false
//                activity?.onBackPressed()
                requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(requireActivity(), backPressedCallback)
        return view
    }

    override fun onDestroy() {
        super.onDestroy()
        println("debug: Fragment destroyed")
//        backPressedCallback.isEnabled = false
        backPressedCallback.remove()
    }

    fun onClick(view: View) {

        if (view == startServiceButton) {
            requireActivity().startService(intent)
        } else if (view == stopServiceButton) {
            unBindService()
            requireActivity().stopService(intent)
        } else if (view == bindButton) {
            bindService()
        } else if (view == unbindButton) {
            unBindService()
        }
    }

    private fun bindService(){
        if (!isBind) {
            appContext.bindService(intent, myViewModel, Context.BIND_AUTO_CREATE)
            isBind = true
        }
    }

    private fun unBindService(){
        if (isBind) {
            appContext.unbindService(myViewModel)
            isBind = false
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(BIND_STATUS_KEY, isBind)
    }
