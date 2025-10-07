package com.example.studymate2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.work.*
import com.example.studymate2.databinding.ActivityMainBinding
import com.example.studymate2.ui.dashboard.DashboardFragment
import com.example.studymate2.ui.planner.PlannerFragment
import com.example.studymate2.ui.resources.ResourcesFragment
import com.example.studymate2.ui.settings.SettingsFragment
import com.example.studymate2.worker.CalendarSyncWorker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    companion object {
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 2001
    }

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

        checkCalendarPermissionAndScheduleSync()
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

    // Permission + sync scheduling
    private fun checkCalendarPermissionAndScheduleSync() {
        val permission = Manifest.permission.READ_CALENDAR
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            scheduleCalendarSync()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(permission),
                CALENDAR_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun scheduleCalendarSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<CalendarSyncWorker>(6, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<CalendarSyncWorker>()
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "calendar_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
        workManager.enqueue(oneTimeRequest) // immediate run for testing
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CALENDAR_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(binding.root, "Calendar access granted", Snackbar.LENGTH_SHORT).show()
                scheduleCalendarSync()
            } else {
                Snackbar.make(binding.root, "Calendar access denied", Snackbar.LENGTH_LONG).show()
            }
        }
    }
}
