package com.example.studymate2.ui.settings

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.studymate2.MainActivity
import com.example.studymate2.R
import com.example.studymate2.databinding.FragmentSettingsBinding
import com.example.studymate2.settings.UserPreferencesRepository
import com.example.studymate2.settings.userPreferencesDataStore
import com.example.studymate2.viewmodel.SettingsViewModel
import com.example.studymate2.viewmodel.SettingsViewModelFactory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModelFactory(UserPreferencesRepository(requireContext().userPreferencesDataStore))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val user = auth.currentUser
        val displayName = user?.displayName?.takeIf { it.isNotBlank() }
            ?: getString(R.string.settings_account_name_placeholder)
        binding.accountName.text = displayName
        binding.displayNameInput.setText(displayName)
        binding.accountEmail.text = user?.email ?: getString(R.string.login_subtitle)

        binding.changeReminderButton.setOnClickListener {
            val settings = viewModel.settings.value
            showTimePicker(settings.reminderHour, settings.reminderMinute)
        }

        binding.updateDisplayNameButton.setOnClickListener {
            val newName = binding.displayNameInput.text?.toString()?.trim().orEmpty()
            if (newName.isBlank()) {
                binding.displayNameInputLayout.error = getString(R.string.settings_display_name_invalid)
            } else {
                binding.displayNameInputLayout.error = null
                updateDisplayName(newName)
            }
        }

        binding.signOutButton.setOnClickListener {
            (activity as? MainActivity)?.promptLogout()
        }

        observeSettings()
    }

    private fun observeSettings() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.settings.collect { settings ->
                    binding.notificationsSwitch.setOnCheckedChangeListener(null)
                    binding.notificationsSwitch.isChecked = settings.notificationsEnabled
                    binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.setNotifications(isChecked)
                        val message = if (isChecked) {
                            R.string.settings_notifications_enabled
                        } else {
                            R.string.settings_notifications_disabled
                        }
                        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    }

                    binding.reminderTime.text = getString(
                        R.string.settings_reminder_updated,
                        settings.reminderHour,
                        settings.reminderMinute
                    )
                }
            }
        }
    }

    private fun showTimePicker(hour: Int, minute: Int) {
        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            viewModel.updateReminder(selectedHour, selectedMinute)
            binding.reminderTime.text = getString(
                R.string.settings_reminder_updated,
                selectedHour,
                selectedMinute
            )
        }, hour, minute, true).show()
    }

    private fun updateDisplayName(newName: String) {
        val user = auth.currentUser ?: return
        val updates = userProfileChangeRequest { displayName = newName }
        user.updateProfile(updates).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.accountName.text = newName
                Snackbar.make(binding.root, R.string.settings_display_name_updated, Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(binding.root, R.string.settings_display_name_invalid, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
