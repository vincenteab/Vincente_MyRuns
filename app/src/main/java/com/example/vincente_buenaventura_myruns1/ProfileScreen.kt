package com.example.vincente_buenaventura_myruns1


import android.app.Activity
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.util.Date


class ProfileScreen : AppCompatActivity() {
    private lateinit var imageView: ImageView
    private lateinit var nameText: EditText
    private lateinit var emailText: EditText
    private lateinit var phoneNumText: EditText
    private lateinit var classText: EditText
    private lateinit var majorText: EditText
    private lateinit var saveButton: Button
    private lateinit var changeButton: Button
    private lateinit var cancelButton: Button
    private lateinit var cameraResult: ActivityResultLauncher<Intent>
    private lateinit var imgUri: Uri
    private lateinit var myViewModel: MyViewModel
    private lateinit var radioGroup: RadioGroup
    private lateinit var imgFileName:String
    private var lastSavedPhotoPath:String = ""
    private var currentSavedPhotoPath:String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.screen_profile)

        imageView = findViewById(R.id.imageViewProfilePhoto)
        nameText = findViewById(R.id.editTextName)
        emailText = findViewById(R.id.editTextEmail)
        phoneNumText = findViewById(R.id.editTextPhone)
        classText = findViewById(R.id.editTextClass)
        majorText  = findViewById(R.id.editTextMajor)
        saveButton = findViewById(R.id.buttonSave)
        changeButton = findViewById(R.id.buttonChange)
        cancelButton = findViewById(R.id.buttonCancel)
        radioGroup = findViewById(R.id.radioGroup)


        loadSavedData();

        Util.checkPermissions(this)
        imgFileName = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date()) + ".jpg"
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

                    //This portion reads EXIF data to see if the image needs to be rotated or not
                    val degrees: Float
                    val exif: ExifInterface = ExifInterface(imgUri.path.toString())
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90){
                        degrees = 90f
                    }else if (orientation == ExifInterface.ORIENTATION_ROTATE_180){
                        degrees = 180f
                    }else if (orientation == ExifInterface.ORIENTATION_ROTATE_270){
                        degrees = 270f
                    }else{
                        degrees = 0f
                    }

                    val bitmap = Util.getBitmap(this, imgUri, degrees)
                    myViewModel.image.value = bitmap
                    currentSavedPhotoPath = imgUri.path.toString()

                }
        }

        myViewModel = ViewModelProvider(this).get(MyViewModel::class.java)
        myViewModel.image.observe(this, { it ->
            imageView.setImageBitmap(it)
        })

        saveButton.setOnClickListener(){
            saveData()
            finish()
        }

        cancelButton.setOnClickListener(){
            if (lastSavedPhotoPath != "") {
                val file = File(lastSavedPhotoPath)
                if (file.exists()) {
                    imageView.setImageURI(Uri.fromFile(file)) // Revert to last saved image
                }
            }
            finish()
        }


        }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        // Store the values from the EditText fields
        editor.putString("text1", nameText.getText().toString())
        editor.putString("text2", emailText.getText().toString())
        editor.putString("text3", phoneNumText.getText().toString())
        editor.putString("text4", classText.getText().toString())
        editor.putString("text5", majorText.getText().toString())

        val selectedRadioButtonId = radioGroup.getCheckedRadioButtonId()
        editor.putInt("selectedRadioButton", selectedRadioButtonId)

        if (currentSavedPhotoPath == ""){
            editor.putString("photoPath", lastSavedPhotoPath)

        }else{

            editor.putString("photoPath", currentSavedPhotoPath)
            lastSavedPhotoPath = currentSavedPhotoPath
        }


        // Apply changes
        editor.apply()
    }
    private fun loadSavedData() {
        val sharedPreferences = getSharedPreferences("MyAppPrefs", MODE_PRIVATE)

        // Retrieve the saved data (if available), and set it back into the EditText fields
        val savedText1 =
            sharedPreferences.getString("text1", "") // Default to empty string if not found
        val savedText2 = sharedPreferences.getString("text2", "")
        val savedText3 = sharedPreferences.getString("text3", "")
        val savedText4 = sharedPreferences.getString("text4", "")
        val savedText5 = sharedPreferences.getString("text5", "")


        nameText.setText(savedText1)
        emailText.setText(savedText2)
        phoneNumText.setText(savedText3)
        classText.setText(savedText4)
        majorText.setText(savedText5)

        val savedRadioButtonId = sharedPreferences.getInt("selectedRadioButton", -1)
        if (savedRadioButtonId != -1) {
            val savedRadioButton = findViewById<RadioButton>(savedRadioButtonId)
            if (savedRadioButton != null) {
                savedRadioButton.isChecked = true // Restore saved RadioButton selection
            }
        }

        lastSavedPhotoPath = sharedPreferences.getString("photoPath", null).toString()
        if (lastSavedPhotoPath != "") {
            val file = File(lastSavedPhotoPath)
            if (file.exists()) {
                imageView.setImageURI(Uri.fromFile(file))
            }
        }
    }
    }
