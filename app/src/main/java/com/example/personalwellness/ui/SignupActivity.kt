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

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val email: EditText = findViewById(R.id.email)
        val password: EditText = findViewById(R.id.password)
        val confirmPassword: EditText = findViewById(R.id.confirm_password)
        val btnSignup: Button = findViewById(R.id.btn_signup)
        val tvLogin: TextView = findViewById(R.id.tv_login)
        val btnBack: ImageButton = findViewById(R.id.btn_back_signup)

        btnSignup.setOnClickListener {
            // TODO: Save signup details (later with SharedPreferences or Firebase)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        // Back button → go to Login
        btnBack.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
