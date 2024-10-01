Vincente Buenaventura
301422086
CMPT 362 D100
September 19, 2024

The purpose of this read.me file is to showcase the code that I used in my assignment that is from the lectures.
Below lists the code that I used from lectures and in my code


Util.kt file:
package com.example.vincente_buenaventura_myruns1

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object Util {
    fun checkPermissions(activity: Activity?) {
        if (Build.VERSION.SDK_INT < 23) return

        if (ContextCompat.checkSelfPermission(activity!!, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.CAMERA), 0)
        }
    }

    fun getBitmap(context: Context, imgUri: Uri): Bitmap {
        var bitmap = BitmapFactory.decodeStream(context.contentResolver.openInputStream(imgUri))
        val matrix = Matrix()
        matrix.setRotate(90f)
        var ret = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        return ret
    }
}



MyViewModel.kt:
package com.example.vincente_buenaventura_myruns1

import android.graphics.Bitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MyViewModel:ViewModel() {
    val image = MutableLiveData<Bitmap>()
}




Code from ProfileScreen.kt file that includes functionality of smartphone camera and display of image:
 val imgFile = File(getExternalFilesDir(null), imgFileName)

        imgUri = FileProvider.getUriForFile(this, "com.example.vincente_buenaventura_myruns1",
            imgFile)

        changeButton.setOnClickListener(){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
            cameraResult.launch(intent)
        }

        cameraResult = registerForActivityResult(StartActivityForResult()){
            result: ActivityResult ->
                if(result.resultCode == Activity.RESULT_OK){
                    val bitmap = Util.getBitmap(this, imgUri)
                    myViewModel.image.value = bitmap
                    currentSavedPhotoPath = imgUri.path.toString()

                }
        }

        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.image.observe(this, { it ->
            imageView.setImageBitmap(it)
        })