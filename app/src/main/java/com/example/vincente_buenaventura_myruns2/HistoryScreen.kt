package com.example.vincente_buenaventura_myruns2

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import java.util.*
import kotlin.collections.ArrayList
import kotlin.random.Random.Default.nextInt


class HistoryScreen : Fragment() {
    private lateinit var myListView: ListView

    private lateinit var arrayList: ArrayList<HistoryEntry>
    private lateinit var arrayAdapter: HistoryAdapter

    private lateinit var database: HistoryDatabase
    private lateinit var databaseDao: HistoryDatabaseDao
    private lateinit var historyViewModel: HistoryViewModel
    private lateinit var repo: HistoryRepo
    private lateinit var factory: HistoryViewModelFactory
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.screen_history, container, false)
        myListView = view.findViewById(R.id.list)



        arrayList = ArrayList()
        arrayAdapter = HistoryAdapter(requireActivity(), arrayList)
        myListView.adapter = arrayAdapter
        database = HistoryDatabase.getInstance(requireActivity())
        databaseDao = database.historyDatabaseDao
        repo = HistoryRepo(databaseDao)

        factory = HistoryViewModelFactory(repo)
        historyViewModel =
            ViewModelProvider(requireActivity(), factory).get(HistoryViewModel::class.java)


        historyViewModel.allHistoryLiveData.observe(viewLifecycleOwner) { entries ->
            arrayAdapter.replace(entries)
            arrayAdapter.notifyDataSetChanged()
            myListView.invalidateViews()
        }

        myListView.setOnItemClickListener { parent, view, position, id ->
            val selected = parent.getItemAtPosition(position) as HistoryEntry
            if (selected.inputType == 0) {
                val intent = Intent(requireContext(), DisplayEntryActivity::class.java)
                intent.putExtra("id", selected.id)
                intent.putExtra("inputType", selected.inputType)
                intent.putExtra("activityType", selected.activityType)
                intent.putExtra("dateTime", selected.dateTime)
                intent.putExtra("duration", selected.duration)
                intent.putExtra("distance", selected.distance)
                intent.putExtra("calories", selected.calories)
                intent.putExtra("heartRate", selected.heartRate)
                intent.putExtra("units", selected.units)
                startActivity(intent)
            } else if (selected.inputType == 1 || selected.inputType == 2) {
                val intent = Intent(requireContext(), MapDisplayActivity::class.java)
                intent.putExtra("display", true)
                intent.putExtra("id", selected.id)
                intent.putExtra("inputType", selected.inputType)
                intent.putExtra("activityType", selected.activityType)
                intent.putExtra("duration", selected.duration)
                intent.putExtra("distance", selected.distance)
                intent.putExtra("calories", selected.calories)
                intent.putExtra("units", selected.units)
                intent.putExtra("coordinates", selected.coordinates)
                intent.putExtra("avgSpeed", selected.avgSpeed)
                intent.putExtra("units", selected.units)
                startActivity(intent)


            }




        }
        return view
    }

    override fun onResume() {
        super.onResume()
        arrayAdapter.notifyDataSetChanged()
    }


}