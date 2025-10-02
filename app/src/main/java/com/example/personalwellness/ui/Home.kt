package com.example.personalwellness.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.personalwellness.R
import org.json.JSONArray

class Home : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Header buttons
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        val btnSettings = view.findViewById<ImageButton>(R.id.btnSettings)

        // Today's mood card views
        val tvMoodEmoji = view.findViewById<TextView>(R.id.tvTodayMoodEmoji)
        val tvMoodText = view.findViewById<TextView>(R.id.tvTodayMoodText)
        val cardTodayMood = view.findViewById<View>(R.id.cardTodayMood)

        // Load the latest mood from SharedPreferences
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val data = prefs.getString("MOOD_DATA", "[]")
        val jsonArray = JSONArray(data)

        if (jsonArray.length() > 0) {
            val latestMood = jsonArray.getJSONObject(jsonArray.length() - 1)
            val mood = latestMood.getString("mood")

            // Extract emoji (first character(s)) and text part
            val emoji = if (mood.isNotEmpty()) mood.substring(0, 2) else "🙂"
            val moodText = mood.substring(2).trim() // e.g. "Happy", "Sad"

            tvMoodEmoji.text = emoji
            tvMoodText.text = moodText
        } else {
            tvMoodEmoji.text = "🙂"
            tvMoodText.text = "No mood yet"
        }

        // Make the card clickable → open Mood Journal fragment
        cardTodayMood.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Mood())
                .addToBackStack(null)
                .commit()
        }

        // Profile navigation
        btnProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Profile())
                .addToBackStack(null)
                .commit()
        }

        // Settings navigation
        btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Settings())
                .addToBackStack(null)
                .commit()
        }
    }
}
