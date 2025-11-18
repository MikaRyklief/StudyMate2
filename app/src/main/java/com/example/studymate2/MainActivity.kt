package com.example.studymate2

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.*
import com.example.studymate2.databinding.ActivityMainBinding
import com.example.studymate2.notification.StudyNotificationScheduler
import com.example.studymate2.settings.UserPreferencesRepository
import com.example.studymate2.settings.userPreferencesDataStore
import com.example.studymate2.ui.dashboard.DashboardFragment
import com.example.studymate2.ui.planner.PlannerFragment
import com.example.studymate2.ui.resources.ResourcesFragment
import com.example.studymate2.ui.settings.SettingsFragment
import com.example.studymate2.worker.CalendarSyncWorker
import com.example.studymate2.worker.TaskSyncWorker
import com.google.android.material.snackbar.Snackbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val preferencesRepository by lazy {
        UserPreferencesRepository(applicationContext.userPreferencesDataStore)
    }
    private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        GoogleSignIn.getClient(this, gso)
    }
    companion object {
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 2001
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 2002
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        StudyNotificationScheduler.createChannels(this)
        requestNotificationPermissionIfNeeded()

        setupBottomNavigation()

        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.navigation_dashboard
            openFragment(DashboardFragment(), R.string.nav_dashboard)
        }

        observePreferences()
    }

    override fun onStart() {
        super.onStart()
        val user = auth.currentUser
        if (user == null) {
            WorkManager.getInstance(this).cancelUniqueWork("task_sync")
            WorkManager.getInstance(this).cancelUniqueWork("calendar_sync")
            Snackbar.make(binding.root, R.string.auth_required, Snackbar.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {
            checkCalendarPermissionAndScheduleSync()
            scheduleTaskSync()
        }
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

    private fun observePreferences() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferencesRepository.settingsFlow.collect { settings ->
                    StudyNotificationScheduler.scheduleDailyReminder(
                        applicationContext,
                        settings.reminderHour,
                        settings.reminderMinute,
                        settings.notificationsEnabled
                    )
                    StudyNotificationScheduler.scheduleWellnessNudges(
                        applicationContext,
                        settings.wellnessNudgesEnabled
                    )
                }
            }
        }
    }

    fun promptLogout() {
        AlertDialog.Builder(this)
            .setMessage(R.string.settings_sign_out_confirmation)
            .setPositiveButton(R.string.settings_sign_out_positive) { _, _ ->
                googleSignInClient.signOut().addOnCompleteListener {
                    auth.signOut()
                    WorkManager.getInstance(this).cancelUniqueWork("task_sync")
                    WorkManager.getInstance(this).cancelUniqueWork("calendar_sync")
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
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
                Snackbar.make(
                    binding.root,
                    getString(R.string.calendar_permission_granted),
                    Snackbar.LENGTH_SHORT
                ).show()
                scheduleCalendarSync()
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.calendar_permission_denied),
                    Snackbar.LENGTH_LONG
                ).show()
            }
        } else if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                lifecycleScope.launch {
                    val settings = preferencesRepository.settingsFlow.first()
                    StudyNotificationScheduler.scheduleDailyReminder(
                        applicationContext,
                        settings.reminderHour,
                        settings.reminderMinute,
                        settings.notificationsEnabled
                    )
                    StudyNotificationScheduler.scheduleWellnessNudges(
                        applicationContext,
                        settings.wellnessNudgesEnabled
                    )
                    StudyNotificationScheduler.triggerImmediateRefresh(this@MainActivity)
                }
            }
        }
    }

    private fun scheduleTaskSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicRequest = PeriodicWorkRequestBuilder<TaskSyncWorker>(2, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        val oneTimeRequest = OneTimeWorkRequestBuilder<TaskSyncWorker>()
            .setConstraints(constraints)
            .build()

        val workManager = WorkManager.getInstance(this)
        workManager.enqueueUniquePeriodicWork(
            "task_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicRequest
        )
        workManager.enqueue(oneTimeRequest)
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }
    }
}
