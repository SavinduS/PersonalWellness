package com.example.personalwellness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.personalwellness.R
import java.text.SimpleDateFormat
import java.util.*

class Hydration : Fragment() {

    private lateinit var waterHistory: LinearLayout
    private lateinit var tvAmount: TextView
    private var currentIntake = 0
    private val dailyGoal = 3000 // ml
    private var customAmount = 250 // default ml

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.hydration, container, false)

        // Header buttons
        val btnProfile = view.findViewById<ImageButton>(R.id.btnProfile)
        val btnSettings = view.findViewById<ImageButton>(R.id.btnSettings)

        btnProfile.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Profile())
                .addToBackStack(null)
                .commit()
        }

        btnSettings.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, Settings())
                .addToBackStack(null)
                .commit()
        }

        // Hydration UI
        waterHistory = view.findViewById(R.id.waterHistory)
        tvAmount = view.findViewById(R.id.tvAmount)

        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val btnAddCustom = view.findViewById<Button>(R.id.btnAddCustom)

        updateAmountLabel()

        // Decrease amount
        btnDecrease.setOnClickListener {
            if (customAmount > 50) {
                customAmount -= 50
                updateAmountLabel()
            }
        }

        // Increase amount
        btnIncrease.setOnClickListener {
            if (customAmount < 1000) {
                customAmount += 50
                updateAmountLabel()
            }
        }

        // Add custom amount
        btnAddCustom.setOnClickListener {
            addWater(customAmount)
        }

        return view
    }

    private fun updateAmountLabel() {
        tvAmount.text = "${customAmount}ml"

        val btnAddCustom = view?.findViewById<Button>(R.id.btnAddCustom)
        btnAddCustom?.text = "+ Add ${customAmount}ml"
    }

    private fun addWater(amount: Int) {
        currentIntake += amount
        if (currentIntake > dailyGoal) currentIntake = dailyGoal
        addHistoryEntry(amount)
    }

    private fun addHistoryEntry(amount: Int) {
        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        val entry = TextView(requireContext()).apply {
            text = "$time - $amount ml"
            textSize = 14f
            setPadding(8, 4, 8, 4)
        }
        waterHistory.addView(entry, 0) // newest entry on top

        // Remove "No hydration data yet" placeholder if still there
        if (waterHistory.childCount > 1) {
            val firstView = waterHistory.getChildAt(waterHistory.childCount - 1)
            if (firstView is TextView && firstView.text.contains("No hydration data yet")) {
                waterHistory.removeView(firstView)
            }
        }
    }
}
