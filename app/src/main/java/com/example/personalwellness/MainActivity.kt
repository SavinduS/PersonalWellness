package com.example.personalwellness

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.personalwellness.ui.Home
import com.example.personalwellness.ui.Habits
import com.example.personalwellness.ui.Mood
import com.example.personalwellness.ui.Hydration

class MainActivity : AppCompatActivity() {

    private var backPressedTime: Long = 0
    private var toast: Toast? = null
    private val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ✅ Request POST_NOTIFICATIONS permission for Android 13+
        requestNotificationPermission()

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

    /** ✅ Requests notification permission at runtime (Android 13 +) */
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (ContextCompat.checkSelfPermission(this, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(permission),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }

    /** ✅ Handle result of the permission request */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Notification permission denied — reminders may not work",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /** ✅ Double back press to exit */
    override fun onBackPressed() {
        if (backPressedTime + 2000 > System.currentTimeMillis()) {
            toast?.cancel()
            super.onBackPressed()
            finishAffinity()
        } else {
            toast = Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT)
            toast?.show()
        }
        backPressedTime = System.currentTimeMillis()
    }
}
