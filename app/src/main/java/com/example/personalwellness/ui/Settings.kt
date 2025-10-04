package com.example.personalwellness.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.personalwellness.R

class Settings : Fragment() {

    private lateinit var switchNotifications: Switch
    private lateinit var btnLogout: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        switchNotifications = view.findViewById(R.id.switchNotifications)
        btnLogout = view.findViewById(R.id.btnLogout)

        // ✅ New text items (placeholders)
        val itemLanguage: TextView = view.findViewById(R.id.itemLanguage)
        val itemPermissions: TextView = view.findViewById(R.id.itemPermissions)
        val itemPrivacy: TextView = view.findViewById(R.id.itemPrivacy)
        val itemAbout: TextView = view.findViewById(R.id.itemAbout)

        val prefs = requireContext().getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val btnBack: View = view.findViewById(R.id.btnBack)
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Load saved state for notifications
        val notificationsEnabled = prefs.getBoolean("notifications", true)
        switchNotifications.isChecked = notificationsEnabled

        // ✅ Notifications toggle
        switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            editor.putBoolean("notifications", isChecked).apply()
            Toast.makeText(requireContext(),
                if (isChecked) "Notifications Enabled" else "Notifications Disabled",
                Toast.LENGTH_SHORT
            ).show()
        }

        // ✅ Placeholder clicks
        itemLanguage.setOnClickListener {
            Toast.makeText(requireContext(), "Language settings clicked", Toast.LENGTH_SHORT).show()
        }
        itemPermissions.setOnClickListener {
            Toast.makeText(requireContext(), "Permissions settings clicked", Toast.LENGTH_SHORT).show()
        }
        itemPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Privacy settings clicked", Toast.LENGTH_SHORT).show()
        }
        itemAbout.setOnClickListener {
            Toast.makeText(requireContext(), "About clicked", Toast.LENGTH_SHORT).show()
        }

        // ✅ Logout logic
        btnLogout.setOnClickListener {
            prefs.edit().clear().apply()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}
