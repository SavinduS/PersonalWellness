package com.example.personalwellness.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.personalwellness.MainActivity
import com.example.personalwellness.R
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button
    private lateinit var btnBack: Button
    private lateinit var btnGetStarted: Button
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ If already logged in → skip onboarding
        val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        if (prefs.getBoolean("is_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_onboarding)

        // Views
        viewPager = findViewById(R.id.viewPager)
        btnSkip = findViewById(R.id.btn_skip)
        btnNext = findViewById(R.id.btn_next)
        btnBack = findViewById(R.id.btn_back)
        btnGetStarted = findViewById(R.id.btn_get_started)
        tabLayout = findViewById(R.id.tabLayout)

        // Fragments
        val fragments = listOf(
            OnboardingFragment.newInstance(
                R.drawable.ic_habits,
                getString(R.string.onboarding_title1),
                getString(R.string.onboarding_desc1)
            ),
            OnboardingFragment.newInstance(
                R.drawable.ic_mood,
                getString(R.string.onboarding_title2),
                getString(R.string.onboarding_desc2)
            ),
            OnboardingFragment.newInstance(
                R.drawable.ic_water,
                getString(R.string.onboarding_title3),
                getString(R.string.onboarding_desc3)
            )
        )

        val adapter = OnboardingAdapter(this, fragments)
        viewPager.adapter = adapter

        // ✅ Start at requested page (default = 0)
        val startPage = intent.getIntExtra("startPage", 0)
        viewPager.setCurrentItem(startPage, false)

        // Attach dots (TabLayout)
        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()

        // ✅ Force initial UI update
        updateUIForPage(startPage, fragments.size)

        // Skip → Login
        btnSkip.setOnClickListener { goToLogin() }

        // Next → forward
        btnNext.setOnClickListener {
            if (viewPager.currentItem < fragments.size - 1) {
                viewPager.currentItem += 1
            }
        }

        // Back → previous
        btnBack.setOnClickListener {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        // Get Started → Login
        btnGetStarted.setOnClickListener { goToLogin() }

        // Update buttons on page change
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateUIForPage(position, fragments.size)
            }
        })
    }

    // ✅ Unified function for button/dot visibility
    private fun updateUIForPage(position: Int, totalPages: Int) {
        when (position) {
            0 -> { // First page
                btnSkip.visibility = View.VISIBLE
                btnBack.visibility = View.GONE
                btnNext.visibility = View.VISIBLE
                btnGetStarted.visibility = View.GONE
                tabLayout.visibility = View.VISIBLE
            }
            totalPages - 1 -> { // Last page
                btnSkip.visibility = View.GONE
                btnBack.visibility = View.VISIBLE
                btnNext.visibility = View.GONE
                btnGetStarted.visibility = View.VISIBLE
                tabLayout.visibility = View.GONE
            }
            else -> { // Middle pages
                btnSkip.visibility = View.VISIBLE
                btnBack.visibility = View.VISIBLE
                btnNext.visibility = View.VISIBLE
                btnGetStarted.visibility = View.GONE
                tabLayout.visibility = View.VISIBLE
            }
        }
    }

    // ✅ Handle system back button
    override fun onBackPressed() {
        if (viewPager.currentItem > 0) {
            viewPager.currentItem -= 1
        } else {
            super.onBackPressed()
        }
    }

    // ✅ Central navigation to LoginActivity
    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
