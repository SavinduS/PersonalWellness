package com.example.personalwellness.ui

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.personalwellness.R
import com.google.android.material.tabs.TabLayout
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class Mood : Fragment() {

    private lateinit var noteInput: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var recentMoodsLayout: LinearLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var calendarView: MaterialCalendarView

    private var selectedMood: String? = null
    private val moodKey = "MOOD_DATA"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.mood, container, false)

        noteInput = view.findViewById(R.id.noteInput)
        saveButton = view.findViewById(R.id.btnSaveMood)
        cancelButton = view.findViewById(R.id.btnCancelMood)
        recentMoodsLayout = view.findViewById(R.id.recentMoodsLayout)
        tabLayout = view.findViewById(R.id.moodTabLayout)
        calendarView = view.findViewById(R.id.moodCalendarView)

        val moodOptionsLayout = view.findViewById<GridLayout>(R.id.moodOptionsLayout)

        // Mood options listener
        for (i in 0 until moodOptionsLayout.childCount) {
            val moodView = moodOptionsLayout.getChildAt(i) as TextView
            moodView.setOnClickListener {
                resetMoodSelection(moodOptionsLayout)
                moodView.setBackgroundResource(R.drawable.mood_selected_bg)
                selectedMood = moodView.text.toString()
            }
        }

        saveButton.setOnClickListener { saveMood() }
        cancelButton.setOnClickListener { resetInputs() }

        // Tabs
        tabLayout.addTab(tabLayout.newTab().setText("List"))
        tabLayout.addTab(tabLayout.newTab().setText("Calendar"))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                if (tab.position == 0) {
                    recentMoodsLayout.visibility = View.VISIBLE
                    calendarView.visibility = View.GONE
                } else {
                    recentMoodsLayout.visibility = View.GONE
                    calendarView.visibility = View.VISIBLE
                    decorateCalendarWithMoods()
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadRecentMoods()

        return view
    }

    private fun resetMoodSelection(layout: GridLayout) {
        for (i in 0 until layout.childCount) {
            layout.getChildAt(i).setBackgroundResource(0)
        }
    }

    private fun saveMood() {
        if (selectedMood == null) {
            Toast.makeText(requireContext(), "Please select a mood", Toast.LENGTH_SHORT).show()
            return
        }

        val note = noteInput.text.toString()
        // ✅ Save full date + time
        val timeStamp = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())

        val moodObj = JSONObject().apply {
            put("mood", selectedMood)
            put("note", note)
            put("time", timeStamp)
        }

        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val existingData = prefs.getString(moodKey, "[]")
        val jsonArray = JSONArray(existingData)
        jsonArray.put(moodObj)

        prefs.edit().putString(moodKey, jsonArray.toString()).apply()

        Toast.makeText(requireContext(), "Mood saved!", Toast.LENGTH_SHORT).show()
        resetInputs()
        loadRecentMoods()
    }

    private fun resetInputs() {
        noteInput.setText("")
        selectedMood = null
        resetMoodSelection(requireView().findViewById(R.id.moodOptionsLayout))
    }

    private fun loadRecentMoods() {
        recentMoodsLayout.removeAllViews()
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val data = prefs.getString(moodKey, "[]")
        val jsonArray = JSONArray(data)

        for (i in jsonArray.length() - 1 downTo 0) { // latest first
            val obj = jsonArray.getJSONObject(i)
            val mood = obj.getString("mood")
            val note = obj.getString("note")
            val time = obj.getString("time")

            val card = LayoutInflater.from(requireContext())
                .inflate(R.layout.mood_item, recentMoodsLayout, false)

            val moodText = card.findViewById<TextView>(R.id.moodText)
            val moodNote = card.findViewById<TextView>(R.id.moodNote)
            val moodTime = card.findViewById<TextView>(R.id.moodTime)
            val btnEdit = card.findViewById<ImageButton>(R.id.btnEditMood)
            val btnDelete = card.findViewById<ImageButton>(R.id.btnDeleteMood)

            moodText.text = mood
            moodNote.text = if (note.isNotEmpty()) note else "(No note)"
            moodTime.text = time

            btnEdit.setOnClickListener { editMood(i, obj) }
            btnDelete.setOnClickListener { deleteMood(i) }

            recentMoodsLayout.addView(card)
        }
    }

    private fun editMood(index: Int, moodObj: JSONObject) {
        selectedMood = moodObj.getString("mood")
        noteInput.setText(moodObj.getString("note"))

        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val data = prefs.getString(moodKey, "[]")
        val jsonArray = JSONArray(data)
        val newArray = JSONArray()
        for (i in 0 until jsonArray.length()) {
            if (i != index) newArray.put(jsonArray.getJSONObject(i))
        }
        prefs.edit().putString(moodKey, newArray.toString()).apply()
        loadRecentMoods()
    }

    private fun deleteMood(index: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Mood")
            .setMessage("Are you sure you want to delete this mood?")
            .setPositiveButton("Yes") { _, _ ->
                val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
                val data = prefs.getString(moodKey, "[]")
                val jsonArray = JSONArray(data)
                val newArray = JSONArray()
                for (i in 0 until jsonArray.length()) {
                    if (i != index) newArray.put(jsonArray.getJSONObject(i))
                }
                prefs.edit().putString(moodKey, newArray.toString()).apply()
                loadRecentMoods()
            }
            .setNegativeButton("No", null)
            .show()
    }

    // ✅ Safe Calendar integration
    private fun decorateCalendarWithMoods() {
        val prefs = requireContext().getSharedPreferences("mood_prefs", Context.MODE_PRIVATE)
        val data = prefs.getString(moodKey, "[]")
        val jsonArray = JSONArray(data)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val moodMap = mutableMapOf<CalendarDay, String>()

        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val mood = obj.getString("mood")
            val dateTime = obj.optString("time", "")
            if (dateTime.isEmpty()) continue

            try {
                val dateOnly = dateTime.split(" ")[0] // extract "dd/MM/yyyy"
                val parsedDate = sdf.parse(dateOnly) ?: continue
                val calendar = Calendar.getInstance()
                calendar.time = parsedDate
                val day = CalendarDay.from(calendar)

                val emoji = mood.take(2) // safely get emoji
                moodMap[day] = emoji
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Clear old decorators
        calendarView.removeDecorators()

        // Add new decorators
        for ((day, emoji) in moodMap) {
            calendarView.addDecorator(MoodDecorator(setOf(day), emoji))
        }
    }
}
