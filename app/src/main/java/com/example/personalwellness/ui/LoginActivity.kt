package com.example.personalwellness.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.personalwellness.MainActivity
import com.example.personalwellness.R

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val btnLogin: Button = findViewById(R.id.btn_login)
        val tvSignup: TextView = findViewById(R.id.tv_signup)
        val btnBack: ImageButton = findViewById(R.id.btn_back_login)

        // ✅ Navigate to MainActivity after login
        btnLogin.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // ✅ Switch to Signup
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }

        // ✅ Back → Onboarding (page 3)
        btnBack.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            intent.putExtra("startPage", 2) // 0=page1, 1=page2, 2=page3
            startActivity(intent)
            finish()
        }
    }
}
