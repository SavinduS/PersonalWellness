package com.example.personalwellness.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.personalwellness.R

class Habits : Fragment() {

    // Data class to hold habit info
    data class Habit(
        val emoji: String,
        val name: String,
        val goal: Int, // in minutes or units
        val unit: String
    )

    // List of habits
    private val habits = listOf(
        Habit("🏃", "Exercise", 30, "minutes"),
        Habit("🧘", "Meditation", 10, "minutes"),
        Habit("📚", "Reading", 20, "minutes"),
        Habit("😴", "Sleep", 8, "hours")
    )

    // Track progress
    private val progressMap = mutableMapOf<String, Int>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.habits, container, false)

        // Initialize progress map
        habits.forEach { habit -> progressMap[habit.name] = 0 }

        // Setup each habit card
        setupHabitCard(view, R.id.habitExercise, habits[0])
        setupHabitCard(view, R.id.habitMeditation, habits[1])
        setupHabitCard(view, R.id.habitReading, habits[2])
        setupHabitCard(view, R.id.habitSleep, habits[3])

        return view
    }

    private fun setupHabitCard(view: View, cardId: Int, habit: Habit) {
        val cardView = view.findViewById<View>(cardId)

        val emoji = cardView.findViewById<TextView>(R.id.habitEmoji)
        val name = cardView.findViewById<TextView>(R.id.habitName)
        val goal = cardView.findViewById<TextView>(R.id.habitGoal)
        val progress = cardView.findViewById<ProgressBar>(R.id.habitProgress)
        val counter = cardView.findViewById<TextView>(R.id.habitCounter)
        val status = cardView.findViewById<TextView>(R.id.habitStatus)
        val btnDecrease = cardView.findViewById<Button>(R.id.btnHabitDecrease)
        val btnIncrease = cardView.findViewById<Button>(R.id.btnHabitIncrease)
        val checkBox = cardView.findViewById<CheckBox>(R.id.habitCheckBox)

        // Initialize UI
        emoji.text = habit.emoji
        name.text = habit.name
        goal.text = "Goal: ${habit.goal} ${habit.unit}"
        progress.max = habit.goal
        counter.text = "0"
        status.text = "0 / ${habit.goal} ${habit.unit}"

        // Update UI helper
        fun updateUI() {
            val value = progressMap[habit.name] ?: 0
            progress.progress = value
            counter.text = value.toString()
            status.text = "$value / ${habit.goal} ${habit.unit}"
            checkBox.isChecked = (value >= habit.goal)
        }

        // Button actions
        btnDecrease.setOnClickListener {
            val current = progressMap[habit.name] ?: 0
            if (current > 0) {
                progressMap[habit.name] = current - 1
                updateUI()
            }
        }

        btnIncrease.setOnClickListener {
            val current = progressMap[habit.name] ?: 0
            if (current < habit.goal) {
                progressMap[habit.name] = current + 1
                updateUI()
            }
        }

        checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                progressMap[habit.name] = habit.goal
            } else {
                // Reset to current value if unchecked
                val current = progressMap[habit.name] ?: 0
                if (current >= habit.goal) {
                    progressMap[habit.name] = habit.goal - 1
                }
            }
            updateUI()
        }

        // Initial update
        updateUI()
    }
}
