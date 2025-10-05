package com.example.personalwellness.ui

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.personalwellness.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Habits : Fragment() {

    // Data class to hold habit info
    data class Habit(
        var emoji: String = "✨",
        var name: String,
        var goal: Int,
        var unit: String,
        var progress: Int = 0
    )

    private val habits = mutableListOf<Habit>()
    private lateinit var prefs: SharedPreferences
    private lateinit var habitContainer: LinearLayout
    private val gson = Gson()

    private var progressCircle: CircularProgressIndicator? = null
    private var tvHabitsStatus: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.habits, container, false)

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

        prefs = requireContext().getSharedPreferences("habits_prefs", Context.MODE_PRIVATE)
        habitContainer = view.findViewById(R.id.habitListContainer)

        progressCircle = view.findViewById(R.id.habitsProgress)
        tvHabitsStatus = view.findViewById(R.id.tvHabitsStatus)

        // Load saved habits
        loadHabits()

        // Add Habit button
        val btnAddHabit = view.findViewById<FloatingActionButton>(R.id.btnAddHabit)
        btnAddHabit.setOnClickListener { openAddHabitDialog(null) }

        // Render current habits
        renderHabits()

        return view
    }

    // Render all habits dynamically
    private fun renderHabits() {
        habitContainer.removeAllViews()
        for (habit in habits) {
            val cardView = layoutInflater.inflate(R.layout.item_habit_card, habitContainer, false)

            val emoji = cardView.findViewById<TextView>(R.id.habitEmoji)
            val name = cardView.findViewById<TextView>(R.id.habitName)
            val goal = cardView.findViewById<TextView>(R.id.habitGoal)
            val progressBar = cardView.findViewById<ProgressBar>(R.id.habitProgress)
            val counter = cardView.findViewById<TextView>(R.id.habitCounter)
            val status = cardView.findViewById<TextView>(R.id.habitStatus)
            val btnDecrease = cardView.findViewById<Button>(R.id.btnHabitDecrease)
            val btnIncrease = cardView.findViewById<Button>(R.id.btnHabitIncrease)
            val checkBox = cardView.findViewById<CheckBox>(R.id.habitCheckBox)
            val btnEdit = cardView.findViewById<ImageButton>(R.id.btnEditHabit)
            val btnDelete = cardView.findViewById<ImageButton>(R.id.btnDeleteHabit)

            // Initialize UI
            emoji.text = habit.emoji
            name.text = habit.name
            goal.text = "Goal: ${habit.goal} ${habit.unit}"
            progressBar.max = habit.goal

            fun updateUI() {
                progressBar.progress = habit.progress
                counter.text = habit.progress.toString()
                status.text = "${habit.progress} / ${habit.goal} ${habit.unit}"
                checkBox.isChecked = (habit.progress >= habit.goal)
                saveHabits()
                updateOverallProgress()
            }

            // Button actions
            btnDecrease.setOnClickListener {
                if (habit.progress > 0) {
                    habit.progress--
                    updateUI()
                }
            }
            btnIncrease.setOnClickListener {
                if (habit.progress < habit.goal) {
                    habit.progress++
                    updateUI()
                }
            }

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    habit.progress = habit.goal
                } else {
                    if (habit.progress >= habit.goal) {
                        habit.progress = habit.goal - 1
                    }
                }
                updateUI()
            }

            // Edit Habit
            btnEdit.setOnClickListener {
                openAddHabitDialog(habit)
            }

            // Delete Habit
            btnDelete.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete '${habit.name}'?")
                    .setPositiveButton("Yes") { _, _ ->
                        habits.remove(habit)
                        saveHabits()
                        renderHabits()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Initial update
            updateUI()

            habitContainer.addView(cardView)
        }
        updateOverallProgress()
    }

    // Update overall progress for Today’s Progress card
    private fun updateOverallProgress() {
        val completed = habits.count { it.progress >= it.goal }
        val remaining = habits.size - completed
        val percentage = if (habits.isNotEmpty()) (completed * 100) / habits.size else 0

        progressCircle?.progress = percentage
        tvHabitsStatus?.text = "$completed Completed • $remaining Remaining"
    }

    // Dialog for Add/Edit
    private fun openAddHabitDialog(existingHabit: Habit?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_habit, null)
        val etName = dialogView.findViewById<EditText>(R.id.etHabitName)
        val etGoal = dialogView.findViewById<EditText>(R.id.etHabitGoal)
        val etUnit = dialogView.findViewById<EditText>(R.id.etHabitUnit)

        if (existingHabit != null) {
            etName.setText(existingHabit.name)
            etGoal.setText(existingHabit.goal.toString())
            etUnit.setText(existingHabit.unit)
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (existingHabit == null) "Add Habit" else "Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val goal = etGoal.text.toString().toIntOrNull() ?: 1
                val unit = etUnit.text.toString().trim()

                if (name.isNotEmpty() && unit.isNotEmpty()) {
                    if (existingHabit == null) {
                        habits.add(Habit("✨", name, goal, unit))
                    } else {
                        existingHabit.name = name
                        existingHabit.goal = goal
                        existingHabit.unit = unit
                        if (existingHabit.progress > goal) {
                            existingHabit.progress = goal
                        }
                    }
                    saveHabits()
                    renderHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Save to SharedPreferences
    private fun saveHabits() {
        val json = gson.toJson(habits)
        prefs.edit().putString("habit_list", json).apply()
    }

    // Load from SharedPreferences
    private fun loadHabits() {
        val json = prefs.getString("habit_list", null) ?: return
        val type = object : TypeToken<MutableList<Habit>>() {}.type
        habits.clear()
        habits.addAll(gson.fromJson(json, type))
    }
}
