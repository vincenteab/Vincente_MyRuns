package com.example.vincente_buenaventura_myruns2

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel:ViewModel() {
    val image = MutableLiveData<Bitmap>()
}