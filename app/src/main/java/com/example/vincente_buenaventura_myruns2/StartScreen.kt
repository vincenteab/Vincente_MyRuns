package com.example.vincente_buenaventura_myruns2


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment


class StartScreen : Fragment() {
    private lateinit var selectedItem: String
    private lateinit var acti: Activity
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view:View = inflater.inflate(R.layout.screen_start, container, false)

        val spinner: Spinner = view.findViewById(R.id.spinner)
        val spinnerItems:List<String> = listOf("Manual Entry", "GPS", "Automatic")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedItem = parent.getItemAtPosition(position).toString()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val spinner2: Spinner = view.findViewById(R.id.spinner2)
        val spinnerItems2:List<String> = listOf("Running", "Walking", "Standing", "Cycling", "Hiking", "Downhill Skiing", "Cross-Country Skiing", "Snowboarding", "Skating",
            "Swimming", "Mountain Biking", "Wheelchair", "Elliptical", "Other")
        val adapter2 = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems2)
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2

        spinner2.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val startButton: Button = view.findViewById(R.id.button2)
        startButton.setOnClickListener{

            if (selectedItem == "Manual Entry"){
                val intent = Intent(requireContext(), ManualInputActivity::class.java)
                intent.putExtra("inputType", 0)
                intent.putExtra("activityType", spinner2.selectedItemPosition)
                startActivity(intent)
            }else if (selectedItem == "GPS"){

                val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                intent.putExtra("display", false)
                intent.putExtra("inputType", 1)
                intent.putExtra("activityType", spinner2.selectedItemPosition)
                startActivity(intent)
            }else{
                val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                intent.putExtra("display", false)
                intent.putExtra("inputType", 2)
                intent.putExtra("activityType", spinner2.selectedItemPosition)
                startActivity(intent)
            }

        }


        return view
    }
}