package com.example.vincente_buenaventura_myruns2

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider


class SettingsScreen : Fragment() {
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var factory: HistoryViewModelFactory
    private lateinit var repo: HistoryRepo
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var database: HistoryDatabase
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view: View = inflater.inflate(R.layout.screen_settings, container, false)

        database = HistoryDatabase.getInstance(requireContext())
        databaseDao = database.historyDatabaseDao
        repo = HistoryRepo(databaseDao)
        factory = HistoryViewModelFactory(repo)
        historyViewModel = ViewModelProvider(this, factory).get(HistoryViewModel::class.java)

        val profileButton: Button = view.findViewById(R.id.userProfile)
        profileButton.setOnClickListener{
            val intent = Intent(requireContext(), ProfileScreen::class.java)
            startActivity(intent)
        }

        val unitButton: Button = view.findViewById(R.id.button4)

        unitButton.setOnClickListener{
            val infl = LayoutInflater.from(requireContext())
            val dialogView = infl.inflate(R.layout.unit_preference_dialog, null)

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)

            val radioGroup: RadioGroup = dialogView.findViewById(R.id.radio_group)

            builder.setNegativeButton("Cancel") { dialog1, _ ->
                dialog1.dismiss() // Close the dialog
            }

            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("radioButtonPrefs", Context.MODE_PRIVATE)
            val savedRadioButtonId = sharedPreferences.getInt("selectedOption", -1)

            if (savedRadioButtonId != -1) {
                radioGroup.check(savedRadioButtonId)
            }

            val dialog = builder.create()



            radioGroup.setOnCheckedChangeListener{group, checkedId ->


                val editor = sharedPreferences.edit()
                editor.putInt("selectedOption", checkedId)
                editor.apply()




                dialog.dismiss()

            }

            dialog.show()
        }

        val commentsButton: Button = view.findViewById(R.id.button5)

        commentsButton.setOnClickListener{
            val infl = LayoutInflater.from(requireContext())
            val dialogView = infl.inflate(R.layout.comments_dialog, null)

            val builder = AlertDialog.Builder(requireContext())
            builder.setView(dialogView)

            val editText: EditText = dialogView.findViewById(R.id.editTextText)

            val sharedPreferences: SharedPreferences = requireContext().getSharedPreferences("editTextPrefs", Context.MODE_PRIVATE)
            val savedText = sharedPreferences.getString("savedText", "")


            editText.setText(savedText)

            builder.setPositiveButton("OK") { dialog, _ ->
                val editor = sharedPreferences.edit()
                editor.putString("savedText", editText.text.toString())
                editor.apply()
                dialog.dismiss() // Close the dialog
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss() // Close the dialog
            }


            val dialog = builder.create()


            dialog.show()
        }

        val webpageButton: Button = view.findViewById(R.id.button6)

        webpageButton.setOnClickListener{
            val url = "https://www.sfu.ca/computing.html"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)

            startActivity(intent)
        }


        return view
    }
}