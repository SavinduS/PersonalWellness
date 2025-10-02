package com.example.personalwellness.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalwellness.MainActivity
import com.example.personalwellness.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_login)

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val tvSignup: TextView = findViewById(R.id.tv_signup)
        val btnBack: ImageButton = findViewById(R.id.btn_back_login)

        val userPrefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        btnLogin.setOnClickListener {
            val enteredEmail = email.text.toString().trim()
            val enteredPassword = password.text.toString().trim()

            val savedEmail = userPrefs.getString("user_email", null)
            val savedPassword = userPrefs.getString("user_password", null)

            if (enteredEmail == savedEmail && enteredPassword == savedPassword) {
                // ✅ Save logged-in state
                prefs.edit().putBoolean("is_logged_in", true).apply()

                // ✅ Show success toast
                Toast.makeText(this, "Logged in successfully 🎉", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials ❌", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        // ✅ Back button inside the layout (top-left icon)
        btnBack.setOnClickListener {
            // Exit app cleanly instead of going back to Onboarding
            finishAffinity()
        }
    }

    // ✅ Override system back button
    override fun onBackPressed() {
        // Exit app instead of going back to Onboarding
        finishAffinity()
    }
}
