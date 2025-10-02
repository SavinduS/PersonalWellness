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

    companion object {
        private const val PREFS_NAME = "hydration_prefs"
        private const val KEY_CURRENT_INTAKE = "CURRENT_INTAKE"
        private const val KEY_DAILY_GOAL = "DAILY_GOAL"
        private const val KEY_HISTORY_DATA = "HISTORY_DATA"
        private const val KEY_REMINDER_INTERVAL = "REMINDER_INTERVAL_MINUTES"
        private const val DEFAULT_REMINDER_INTERVAL = 60
    }

    private lateinit var waterHistory: LinearLayout
    private lateinit var tvAmount: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var reminderSpinner: Spinner

    private var currentIntake = 0
    private val dailyGoal = 3000 // ml
    private var customAmount = 250 // default ml
    private val reminderIntervals = listOf(15, 30, 45, 60, 90, 120, 180)

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
            scheduleHydrationReminder()
            }

        // Hydration UI
        waterHistory = view.findViewById(R.id.waterHistory)
        tvAmount = view.findViewById(R.id.tvAmount)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)
        reminderSpinner = view.findViewById(R.id.spinnerReminderInterval)

        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val spinnerLabels = reminderIntervals.map { formatIntervalLabel(it) }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerLabels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        reminderSpinner.adapter = adapter

        val savedInterval = prefs.getInt(KEY_REMINDER_INTERVAL, DEFAULT_REMINDER_INTERVAL)
        val initialIndex = reminderIntervals.indexOf(savedInterval).takeIf { it != -1 }
            ?: reminderIntervals.indexOf(DEFAULT_REMINDER_INTERVAL)
        if (initialIndex >= 0) {
            reminderSpinner.setSelection(initialIndex)
        }

        reminderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val interval = reminderIntervals[position]
                prefs.edit().putInt(KEY_REMINDER_INTERVAL, interval).apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val btnDecrease = view.findViewById<Button>(R.id.btnDecrease)
        val btnIncrease = view.findViewById<Button>(R.id.btnIncrease)
        val btnAddCustom = view.findViewById<Button>(R.id.btnAddCustom)
        val btnReset = view.findViewById<Button>(R.id.btnReset)

        updateAmountLabel()

        // 🔹 Load saved hydration progress + history
        val hydrationPrefs = requireContext().getSharedPreferences("hydration_prefs", Context.MODE_PRIVATE)
        currentIntake = hydrationPrefs.getInt(KEY_CURRENT_INTAKE, 0)
        updateProgress()

        val historyJson = hydrationPrefs.getString(KEY_HISTORY_DATA, "[]")
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
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_CURRENT_INTAKE, currentIntake)
            .putInt(KEY_DAILY_GOAL, dailyGoal)
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
            val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val historyJson = prefs.getString(KEY_HISTORY_DATA, "[]")
            val jsonArray = JSONArray(historyJson)

            val obj = JSONObject()
            obj.put("time", time)
            obj.put("amount", amount)

            jsonArray.put(obj)
            prefs.edit().putString(KEY_HISTORY_DATA, jsonArray.toString()).apply()
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
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putInt(KEY_CURRENT_INTAKE, 0)
            .putString(KEY_HISTORY_DATA, "[]")
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
    private fun scheduleHydrationReminder() {
        val context = requireContext()
        val hydrationPrefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        val selectedPosition = reminderSpinner.selectedItemPosition
        val intervalMinutes = if (
            selectedPosition != AdapterView.INVALID_POSITION &&
            selectedPosition in reminderIntervals.indices
        ) {
            reminderIntervals[selectedPosition]
        } else {
            hydrationPrefs.getInt(KEY_REMINDER_INTERVAL, DEFAULT_REMINDER_INTERVAL)
        }

        hydrationPrefs.edit().putInt(KEY_REMINDER_INTERVAL, intervalMinutes).apply()

        val receiverPrefs = context.getSharedPreferences(
            HydrationReceiver.PREFS_NAME,
            Context.MODE_PRIVATE
        )
        receiverPrefs.edit()
            .putInt(HydrationReceiver.KEY_REMINDER_INTERVAL_MINUTES, intervalMinutes)
            .apply()

        val intent = Intent(context, HydrationReceiver::class.java).apply {
            putExtra(HydrationReceiver.KEY_REMINDER_INTERVAL_MINUTES, intervalMinutes)
        }
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            flags
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.cancel(pendingIntent)

        val intervalMillis = intervalMinutes.toLong() * 60_000L
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

        Toast.makeText(
            context,
            "Reminder set for ${formatIntervalDescription(intervalMinutes)}",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun formatIntervalLabel(minutes: Int): String {
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        val parts = mutableListOf<String>()
        if (hours > 0) {
            parts.add(if (hours == 1) "1 hour" else "$hours hours")
        }
        if (remainingMinutes > 0) {
            parts.add("$remainingMinutes minutes")
        }
        if (parts.isEmpty()) {
            parts.add("0 minutes")
        }
        return "Every ${parts.joinToString(" ")}"
    }

    private fun formatIntervalDescription(minutes: Int): String {
        val label = formatIntervalLabel(minutes)
        return label.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    }
}
