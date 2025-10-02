package com.example.personalwellness

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.personalwellness.ui.Home
import com.example.personalwellness.ui.Habits
import com.example.personalwellness.ui.Mood
import com.example.personalwellness.ui.Hydration

class MainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            loadFragment(Home())
            bottomNav.selectedItemId = R.id.nav_home
        }

        bottomNav.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> Home()
                R.id.nav_habits -> Habits()
                R.id.nav_mood -> Mood()
                R.id.nav_hydration -> Hydration()
                else -> Home()
            }
            loadFragment(fragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    // ✅ Double back press to exit
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            toast?.cancel()
            super.onBackPressed()
            finishAffinity() // closes the app
        } else {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
            toast?.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
