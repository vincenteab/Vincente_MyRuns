package com.example.vincente_buenaventura_myruns1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.ArrayList

class Main : AppCompatActivity() {
    private lateinit var startScreen: StartScreen
    private lateinit var profileScreen: ProfileScreen
    private lateinit var fragmentStateAdapter: FragmentStateAdapter
    private lateinit var fragments: ArrayList<Fragment>
    private lateinit var tabConfigStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val tabTitles = arrayOf("Start", "History", "Settings")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        startScreen = StartScreen()
        fragments = ArrayList()
        fragments.add(startScreen)

        fragmentStateAdapter = FragmentStateAdapter(this, fragments)




    }
    override fun onDestroy() {
        super.onDestroy()

    }
}