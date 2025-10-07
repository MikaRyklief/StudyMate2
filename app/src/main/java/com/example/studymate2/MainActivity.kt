package com.example.studymate2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.studymate2.databinding.ActivityMainBinding
import com.example.studymate2.ui.dashboard.DashboardFragment
import com.example.studymate2.ui.planner.PlannerFragment
import com.example.studymate2.ui.resources.ResourcesFragment
import com.example.studymate2.ui.settings.SettingsFragment
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        setupBottomNavigation()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
            openFragment(DashboardFragment(), R.string.nav_dashboard)
        }
    }

    override fun onStart() {
        super.onStart()
        ensureAuthenticated()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_dashboard -> {
                    openFragment(DashboardFragment(), R.string.nav_dashboard)
                    true
                }
                R.id.navigation_planner -> {
                    openFragment(PlannerFragment(), R.string.nav_planner)
                    true
                }
                R.id.navigation_resources -> {
                    openFragment(ResourcesFragment(), R.string.nav_resources)
                    true
                }
                R.id.navigation_settings -> {
                    openFragment(SettingsFragment(), R.string.nav_settings)
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment, titleRes: Int) {
        binding.toolbar.setTitle(titleRes)
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    private fun ensureAuthenticated() {
        val user = auth.currentUser
        if (user == null) {
            Snackbar.make(binding.root, R.string.auth_required, Snackbar.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    fun promptLogout() {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_sign_out_confirmation)
            .setPositiveButton(R.string.settings_sign_out_positive) { _, _ ->
                auth.signOut()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
            .setNegativeButton(R.string.settings_sign_out_negative, null)
            .show()
    }
}
