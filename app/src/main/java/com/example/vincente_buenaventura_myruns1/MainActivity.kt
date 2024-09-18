package com.example.vincente_buenaventura_myruns1


import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import java.io.File


class MainActivity : AppCompatActivity() {
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
    private val imgFileName = "vb.jpg"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById(R.id.imageViewProfilePhoto)
        nameText = findViewById(R.id.editTextName)
        emailText = findViewById(R.id.editTextEmail)
        phoneNumText = findViewById(R.id.editTextPhoneNumber)
        classText = findViewById(R.id.editTextClass)
        majorText  = findViewById(R.id.editTextMajor)
        saveButton = findViewById(R.id.buttonSave)
        changeButton = findViewById(R.id.buttonChange)
        cancelButton = findViewById(R.id.buttonCancel)

        loadSavedData();

        Util.checkPermissions(this)

        val imgFile = File(getExternalFilesDir(null), imgFileName)

        imgUri = FileProvider.getUriForFile(this, "com.example.vincente_buenaventura_myruns1",
            imgFile)

        changeButton.setOnClickListener(){
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri)
            cameraResult.launch(intent)
        }

        cameraResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result: ActivityResult ->
            if(result.resultCode == Activity.RESULT_OK){
                val bitmap = Util.getBitmap(this, imgUri)
                imageView.setImageBitmap(bitmap)
            }
        }

        saveButton.setOnClickListener(){
            saveData();
            finish();
        }

        cancelButton.setOnClickListener(){
            finish();
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
    }
    }
