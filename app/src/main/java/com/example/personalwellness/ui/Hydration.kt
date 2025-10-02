package com.example.personalwellness.ui

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.personalwellness.R
import com.example.personalwellness.receivers.HydrationReceiver
import com.example.personalwellness.utils.NotificationHelper
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Hydration : Fragment() {

    private lateinit var waterHistory: LinearLayout
    private lateinit var tvAmount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView

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
            // For viva demo → set reminder to 1 minute
            scheduleHydrationReminder(1)
            Toast.makeText(requireContext(), "Reminder set for every 1 minute", Toast.LENGTH_SHORT).show()
        }

        // Hydration UI
        waterHistory = view.findViewById(R.id.waterHistory)
        tvAmount = view.findViewById(R.id.tvAmount)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)

        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val btnAddCustom = view.findViewById<Button>(R.id.btnAddCustom)
        val btnReset = view.findViewById<Button>(R.id.btnReset)

        updateAmountLabel()

        // 🔹 Load saved hydration progress + history
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        currentIntake = prefs.getInt("CURRENT_INTAKE", 0)
        updateProgress()

        val historyJson = prefs.getString("HISTORY_DATA", "[]")
        val jsonArray = JSONArray(historyJson)
        if (jsonArray.length() > 0) {
            removePlaceholder()
            for (i in jsonArray.length() - 1 downTo 0) {
                val obj = jsonArray.getJSONObject(i)
                addHistoryEntry(obj.getString("time"), obj.getInt("amount"), save = false)
            }
        }

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

        // Reset hydration data
        btnReset.setOnClickListener {
            resetHydrationData()
        }

        return view
    }

    /** Updates label and button text */
    private fun updateAmountLabel() {
        tvAmount.text = "${customAmount}ml"
        val btnAddCustom = view?.findViewById<Button>(R.id.btnAddCustom)
        btnAddCustom?.text = "+ Add ${customAmount}ml"
    }

    /** Called when water is added */
    private fun addWater(amount: Int) {
        currentIntake += amount
        if (currentIntake > dailyGoal) currentIntake = dailyGoal

        val time = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        addHistoryEntry(time, amount, save = true)
        updateProgress()

        // ✅ Save intake to SharedPreferences
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("CURRENT_INTAKE", currentIntake)
            .putInt("DAILY_GOAL", dailyGoal)
            .apply()

        NotificationHelper(requireContext()).showNotification(
            "Hydration Update",
            "You added $amount ml. Total: $currentIntake / $dailyGoal ml"
        )
    }

    /** Updates progress bar and text */
    private fun updateProgress() {
        val percent = ((currentIntake.toFloat() / dailyGoal) * 100).toInt()
        progressBar.progress = percent
        tvProgress.text = "$percent%"
    }

    /** Adds entry to history, optionally saving it */
    private fun addHistoryEntry(time: String, amount: Int, save: Boolean) {
        removePlaceholder()

        val entry = TextView(requireContext()).apply {
            text = "$time - $amount ml"
            textSize = 14f
            setPadding(8, 4, 8, 4)
        }
        waterHistory.addView(entry, 0)

        if (save) {
            val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
            val historyJson = prefs.getString("HISTORY_DATA", "[]")
            val jsonArray = JSONArray(historyJson)

            val obj = JSONObject()
            obj.put("time", time)
            obj.put("amount", amount)

            jsonArray.put(obj)
            prefs.edit().putString("HISTORY_DATA", jsonArray.toString()).apply()
        }
    }

    /** Removes placeholder (icon + texts) */
    private fun removePlaceholder() {
        val icon = waterHistory.findViewById<ImageView>(R.id.historyIcon)
        val placeholder = waterHistory.findViewById<TextView>(R.id.historyPlaceholder)
        val subtext = waterHistory.findViewById<TextView>(R.id.historySubtext)

        if (icon != null) waterHistory.removeView(icon)
        if (placeholder != null) waterHistory.removeView(placeholder)
        if (subtext != null) waterHistory.removeView(subtext)
    }

    /** Clears today's hydration data */
    private fun resetHydrationData() {
        currentIntake = 0

        // Clear prefs
        val prefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("CURRENT_INTAKE", 0)
            .putString("HISTORY_DATA", "[]")
            .apply()

        // Reset progress
        updateProgress()

        // Clear history views
        waterHistory.removeAllViews()

        // Restore placeholder
        val icon = ImageView(requireContext()).apply {
            id = R.id.historyIcon
            setImageResource(R.drawable.ic_water)
            setColorFilter(resources.getColor(R.color.colorTextSecondary))
            layoutParams = LinearLayout.LayoutParams(48, 48)
        }
        val placeholder = TextView(requireContext()).apply {
            id = R.id.historyPlaceholder
            text = "No hydration data yet"
            textSize = 14f
            setTextColor(resources.getColor(R.color.colorTextSecondary))
            setPadding(0, 8, 0, 0)
        }
        val subtext = TextView(requireContext()).apply {
            id = R.id.historySubtext
            text = "Start tracking your water intake"
            textSize = 12f
            setTextColor(resources.getColor(R.color.colorTextSecondary))
        }

        waterHistory.addView(icon)
        waterHistory.addView(placeholder)
        waterHistory.addView(subtext)

        Toast.makeText(requireContext(), "Hydration data reset!", Toast.LENGTH_SHORT).show()
    }

    /** Schedules hydration reminders with AlarmManager */
    private fun scheduleHydrationReminder(intervalMinutes: Int) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), HydrationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalMinutes * 60 * 1000L
        val triggerTime = System.currentTimeMillis() + intervalMillis

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                pendingIntent
            )
        } else {
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                triggerTime,
                intervalMillis,
                pendingIntent
            )
        }
    }
}
