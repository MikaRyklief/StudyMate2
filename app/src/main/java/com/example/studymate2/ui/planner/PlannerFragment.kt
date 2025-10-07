package com.example.studymate2.ui.planner

import android.Manifest
import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.CalendarContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate2.R
import com.example.studymate2.data.StudyTask
import com.example.studymate2.databinding.DialogAddTaskBinding
import com.example.studymate2.databinding.FragmentPlannerBinding
import com.example.studymate2.viewmodel.StudyTaskViewModel
import com.example.studymate2.viewmodel.StudyTaskViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: StudyTaskViewModel by activityViewModels {
        StudyTaskViewModelFactory(requireActivity().application)
    }

    private lateinit var adapter: StudyTaskAdapter

    companion object {
        private const val CALENDAR_PERMISSION_REQUEST_CODE = 301
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlannerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = StudyTaskAdapter(
            onToggleComplete = { task -> taskViewModel.toggleTaskCompletion(task) },
            onDelete = { task -> confirmDelete(task) }
        )

        binding.tasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.tasksRecycler.adapter = adapter

        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            adapter.submitList(tasks)
            binding.plannerEmptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE
        }

        binding.addTaskFab.setOnClickListener { showAddTaskDialog() }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAddTaskDialog() {
        val dialogBinding = DialogAddTaskBinding.inflate(layoutInflater)
        val calendar = Calendar.getInstance()
        var selectedDateMillis = calendar.timeInMillis
        updateDateLabel(dialogBinding, selectedDateMillis)

        dialogBinding.pickDateButton.setOnClickListener {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    calendar.set(Calendar.YEAR, year)
                    calendar.set(Calendar.MONTH, month)
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    updateDateLabel(dialogBinding, selectedDateMillis)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.planner_add_task)
            .setView(dialogBinding.root)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.setOnShowListener {
            val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            positiveButton.setOnClickListener {
                val title = dialogBinding.inputTaskTitle.text?.toString().orEmpty()
                val subject = dialogBinding.inputTaskSubject.text?.toString().orEmpty()
                val duration = dialogBinding.inputDuration.text?.toString()?.toIntOrNull() ?: 0

                dialogBinding.inputTaskTitle.error = null
                dialogBinding.inputTaskSubject.error = null
                dialogBinding.inputDuration.error = null

                when {
                    title.isBlank() || subject.isBlank() -> {
                        dialogBinding.inputTaskTitle.error =
                            getString(R.string.planner_error_required)
                        dialogBinding.inputTaskSubject.error =
                            getString(R.string.planner_error_required)
                    }

                    duration <= 0 -> {
                        dialogBinding.inputDuration.error =
                            getString(R.string.planner_error_duration)
                    }

                    else -> {
                        taskViewModel.addTask(title, subject, selectedDateMillis, duration)
                        addEventToGoogleCalendar(title, subject, selectedDateMillis, duration)
                        Snackbar.make(binding.root, R.string.planner_saved, Snackbar.LENGTH_SHORT)
                            .show()
                        dialog.dismiss()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun confirmDelete(task: StudyTask) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.planner_delete)
            .setMessage(R.string.planner_delete_confirm)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                taskViewModel.deleteTask(task)
                Snackbar.make(binding.root, R.string.planner_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateDateLabel(binding: DialogAddTaskBinding, timeInMillis: Long) {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        binding.selectedDate.text = formatter.format(timeInMillis)
    }

    // 🔹 Create calendar event
    private fun addEventToGoogleCalendar(
        title: String,
        subject: String,
        startTime: Long,
        durationMinutes: Int
    ) {
        val hasPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_CALENDAR
        ) ==
                PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.WRITE_CALENDAR, Manifest.permission.READ_CALENDAR),
                CALENDAR_PERMISSION_REQUEST_CODE
            )
            Snackbar.make(
                binding.root,
                "Grant calendar permission to sync tasks.",
                Snackbar.LENGTH_LONG
            ).show()
            return
        }

        // 🔹 Step 1: Find a valid calendar ID
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME
        )
        val cursor = requireContext().contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        if (cursor == null) {
            Snackbar.make(binding.root, "No calendar provider found.", Snackbar.LENGTH_LONG).show()
            return
        }

        var calendarId: Long? = null
        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(0)
                val name = it.getString(1)
                println("📅 Found Calendar ID: $id ($name)")
                // Usually you’ll have “Primary” or “My Calendar” — pick first one
                if (name.contains("@gmail.com", ignoreCase = true)) {
                    calendarId = id
                    break
                }
            }

            if (calendarId == null) {
                Snackbar.make(
                    binding.root,
                    "No writable calendars found on device.",
                    Snackbar.LENGTH_LONG
                ).show()
                return
            }

            // 🔹 Step 2: Insert event
            val endTime = startTime + durationMinutes * 60 * 1000
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, "Study Task: $subject")
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri =
                requireContext().contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            if (uri != null) {
                Snackbar.make(binding.root, "Added to Google Calendar!", Snackbar.LENGTH_SHORT)
                    .show()
                println("✅ Calendar event created at URI: $uri")
            } else {
                Snackbar.make(binding.root, "Failed to add event.", Snackbar.LENGTH_SHORT).show()
            }
        }
    }
}
