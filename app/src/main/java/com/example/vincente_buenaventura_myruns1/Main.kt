package com.example.vincente_buenaventura_myruns1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.tabs.TabLayoutMediator.TabConfigurationStrategy
import java.util.ArrayList

class Main : AppCompatActivity() {
    private lateinit var startScreen: StartScreen
    private lateinit var historyScreen: HistoryScreen
    private lateinit var settingsScreen: SettingsScreen
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var fragmentStateAdapter: FragmentStateAdapter
    private lateinit var fragments: ArrayList<Fragment>
    private lateinit var tabConfigStrategy: TabConfigurationStrategy
    private lateinit var tabLayoutMediator: TabLayoutMediator
    private val tabTitles = arrayOf("Start", "History", "Settings")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        viewPager2 = findViewById(R.id.viewPager)
        tabLayout = findViewById(R.id.tab)

        startScreen = StartScreen()
        historyScreen = HistoryScreen()
        settingsScreen = SettingsScreen()
        fragments = ArrayList()
        fragments.add(startScreen)
        fragments.add(historyScreen)
        fragments.add(settingsScreen)

        fragmentStateAdapter = FragmentStateAdapter(this, fragments)
        viewPager2.adapter = fragmentStateAdapter

        tabConfigStrategy = TabConfigurationStrategy{
            tab: TabLayout.Tab, position: Int ->
            tab.text = tabTitles[position] }
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigStrategy)
        tabLayoutMediator.attach()






    }
    override fun onDestroy() {
        super.onDestroy()

    }
}