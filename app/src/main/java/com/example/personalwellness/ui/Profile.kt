package com.example.personalwellness.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.personalwellness.R

class Profile : Fragment() {

    private lateinit var imgAvatar: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var btnBack: ImageButton
    private lateinit var btnChangePassword: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.profile, container, false)

        // Initialize views
        imgAvatar = view.findViewById(R.id.imgAvatar)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        btnLogout = view.findViewById(R.id.btnLogout)
        btnBack = view.findViewById(R.id.btnBack)
        btnChangePassword = view.findViewById(R.id.btnChangePassword)

        // Load user data dynamically
        loadUserData()

        // Button actions
        btnLogout.setOnClickListener {
            Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show()
            // TODO: Add navigation to login or clear session if needed
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnChangePassword.setOnClickListener {
            Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadUserData() {
        val sharedPreferences = activity?.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        // Fetch stored values or use defaults
        val userName = sharedPreferences?.getString("USER_NAME", "Savindu") ?: "Savindu"
        val userEmail = sharedPreferences?.getString("USER_EMAIL", "savindu@gmail.com") ?: "savindu@example.com"

        tvUserName.text = userName
        tvUserEmail.text = userEmail
    }
}
