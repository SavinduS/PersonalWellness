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

        val prefs = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        btnSignup.setOnClickListener {
            val enteredEmail = email.text.toString().trim()
            val enteredPassword = password.text.toString().trim()
            val enteredConfirm = confirmPassword.text.toString().trim()

            if (enteredEmail.isEmpty()) {
                email.error = "Enter email"
                return@setOnClickListener
            }
            if (enteredPassword.isEmpty()) {
                password.error = "Enter password"
                return@setOnClickListener
            }
            if (enteredPassword != enteredConfirm) {
                confirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            // ✅ Save user details in SharedPreferences
            prefs.edit()
                .putString("user_email", enteredEmail)
                .putString("user_password", enteredPassword)
                .apply()

            // ✅ Show success toast
            Toast.makeText(this, "Account created successfully 🎉 Please log in", Toast.LENGTH_LONG).show()

            // Redirect to Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Already have an account → go to Login
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
