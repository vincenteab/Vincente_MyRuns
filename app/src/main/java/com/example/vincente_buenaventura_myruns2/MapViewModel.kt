package com.example.vincente_buenaventura_myruns2

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class MapViewModel : ViewModel(), ServiceConnection {

    private var notifyService: NotifyService? = null
    private var isBound = false
    private val _distance = MutableLiveData<Double>()
    val distance: LiveData<Double> get() = _distance

    private val _averageSpeed = MutableLiveData<Double>()
    val averageSpeed: LiveData<Double> get() = _averageSpeed

    private val _currentSpeed = MutableLiveData<Double>()
    val currentSpeed: LiveData<Double> get() = _currentSpeed

    private val _locationList = MutableLiveData<LatLng>()
    val locationList: LiveData<LatLng> get() = _locationList

    private val _calories = MutableLiveData<Double>()
    val caloriesBurned: LiveData<Double> get() = _calories

    private val _timeElapsed = MutableLiveData<Double>()
    val timeElapsed: LiveData<Double> get() = _timeElapsed

    private val _activityType = MutableLiveData<String>()
    val activityType: LiveData<String> get() = _activityType



    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as NotifyService.MyBinder

        notifyService = binder.getService()
        isBound = true
        // Listen to updates from the service

        notifyService?.let { service ->

            service.distanceLiveData.observeForever { _distance.postValue(it) }
            service.averageSpeedLiveData.observeForever { _averageSpeed.postValue(it) }
            service.currentSpeedLiveData.observeForever { _currentSpeed.postValue(it) }
            service.locationListLiveData.observeForever { _locationList.postValue(it) }
            service.caloriesLiveData.observeForever { _calories.postValue(it) }
            service.timeElapsedLiveData.observeForever { _timeElapsed.postValue(it) }
            service.activityTypeLiveData.observeForever { _activityType.postValue(it) }

        }
        println("debug: onServiceConnected called")
    }

    override fun onServiceDisconnected(name: ComponentName) {
        // Do nothing
        notifyService = null
        isBound = false
        println("debug: onServiceDisconnected called")
    }


    fun bindService(context: Context) {
        val intent = Intent(context, NotifyService::class.java)
        context.startService(intent)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        println("debug: bindService called")
    }

    fun unbindService(context: Context) {
        if (isBound) {
            context.unbindService(this)
            isBound = false
            println("debug: unbindService called")
        }
    }
    override fun onCleared() {
        super.onCleared()
        // Ensure no leaks
        notifyService = null
    }


}