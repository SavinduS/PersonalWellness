package com.example.personalwellness.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.example.personalwellness.MainActivity
import com.example.personalwellness.R
import org.json.JSONArray

class WellnessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        private const val HYDRATION_PREFS = "hydration_prefs"
        private const val HYDRATION_KEY_CURRENT = "CURRENT_INTAKE"
        private const val HYDRATION_KEY_GOAL = "DAILY_GOAL"

        private const val MOOD_PREFS = "mood_prefs"
        private const val MOOD_KEY_DATA = "MOOD_DATA"

        fun refreshWidget(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, WellnessWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            if (appWidgetIds.isEmpty()) return
            for (appWidgetId in appWidgetIds) {
                updateAppWidget(context, appWidgetManager, appWidgetId)
            }
        }

        private fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_wellness)

            val hydrationInfo = getHydrationInfo(context)
            views.setTextViewText(R.id.widgetWaterValue, hydrationInfo.displayText)
            views.setTextViewText(R.id.widgetWaterPercent, hydrationInfo.percentText)
            views.setProgressBar(
                R.id.widgetWaterProgress,
                hydrationInfo.maxProgress,
                hydrationInfo.currentProgress,
                false
            )

            val moodInfo = getMoodInfo(context)
            views.setTextViewText(R.id.widgetMoodValue, moodInfo.primaryText)
            if (moodInfo.secondaryText.isNullOrBlank()) {
                views.setViewVisibility(R.id.widgetMoodNote, View.GONE)
            } else {
                views.setViewVisibility(R.id.widgetMoodNote, View.VISIBLE)
                views.setTextViewText(R.id.widgetMoodNote, moodInfo.secondaryText)
            }

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetRoot, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun getHydrationInfo(context: Context): HydrationInfo {
            val prefs = context.getSharedPreferences(HYDRATION_PREFS, Context.MODE_PRIVATE)
            val current = prefs.getInt(HYDRATION_KEY_CURRENT, 0)
            val goal = prefs.getInt(HYDRATION_KEY_GOAL, 3000)
            val safeGoal = if (goal <= 0) 1 else goal
            val percent = ((current.toFloat() / safeGoal) * 100).toInt().coerceIn(0, 100)
            val displayText = context.getString(
                R.string.widget_water_value,
                current,
                goal
            )
            val percentText = context.getString(R.string.widget_water_percent, percent)
            return HydrationInfo(
                displayText = displayText,
                percentText = percentText,
                currentProgress = percent,
                maxProgress = 100
            )
        }

        private fun getMoodInfo(context: Context): MoodInfo {
            val prefs = context.getSharedPreferences(MOOD_PREFS, Context.MODE_PRIVATE)
            val stored = prefs.getString(MOOD_KEY_DATA, "[]") ?: "[]"
            return try {
                val jsonArray = JSONArray(stored)
                if (jsonArray.length() == 0) {
                    MoodInfo(
                        primaryText = context.getString(R.string.widget_mood_empty),
                        secondaryText = null
                    )
                } else {
                    val obj = jsonArray.getJSONObject(jsonArray.length() - 1)
                    val mood = obj.optString(
                        "mood",
                        context.getString(R.string.widget_mood_empty)
                    )
                    val note = obj.optString("note").takeIf { it.isNotBlank() }
                    val time = obj.optString("time")
                    val secondaryText = buildString {
                        note?.let { append(it) }
                        if (time.isNotBlank()) {
                            if (isNotEmpty()) append(" • ")
                            append(time)
                        }
                    }.ifBlank { null }
                    MoodInfo(
                        primaryText = mood,
                        secondaryText = secondaryText
                    )
                }
            } catch (e: Exception) {
                MoodInfo(
                    primaryText = context.getString(R.string.widget_mood_empty),
                    secondaryText = null
                )
            }
        }
    }
}

private data class HydrationInfo(
    val displayText: String,
    val percentText: String,
    val currentProgress: Int,
    val maxProgress: Int
)

private data class MoodInfo(
    val primaryText: String,
    val secondaryText: String?
)
