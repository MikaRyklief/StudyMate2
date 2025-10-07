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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studymate2.R
import com.example.studymate2.data.StudyBlock
import com.example.studymate2.data.StudyTask
import com.example.studymate2.data.TaskType
import com.example.studymate2.databinding.DialogAddTaskBinding
import com.example.studymate2.databinding.FragmentPlannerBinding
import com.example.studymate2.notification.StudyNotificationScheduler
import com.example.studymate2.util.SmartTimetableGenerator
import com.example.studymate2.viewmodel.StudyTaskViewModel
import com.example.studymate2.viewmodel.StudyTaskViewModelFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: StudyTaskViewModel by activityViewModels {
        StudyTaskViewModelFactory(requireActivity().application)
    }

    private lateinit var adapter: StudyTaskAdapter
    private lateinit var weeklyAdapter: WeeklyScheduleAdapter
    private val monthlyAdapter = MonthlyTaskAdapter()
    private var generatedBlocks: List<StudyBlock> = emptyList()
    private var currentTasks: List<StudyTask> = emptyList()
    private var selectedCalendarDay: Long = 0L

    private val dayFormatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())

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
            onToggleComplete = { task ->
                taskViewModel.toggleTaskCompletion(task)
                StudyNotificationScheduler.triggerImmediateRefresh(requireContext())
            },
            onDelete = { task -> confirmDelete(task) }
        )

        binding.tasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.tasksRecycler.adapter = adapter

        weeklyAdapter = WeeklyScheduleAdapter()
        binding.weeklyScheduleRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.weeklyScheduleRecycler.adapter = weeklyAdapter
        attachDragAndDrop()

        binding.calendarTasksRecycler.layoutManager = LinearLayoutManager(requireContext())
        binding.calendarTasksRecycler.adapter = monthlyAdapter

        binding.plannerViewToggle.check(R.id.viewListButton)
        binding.plannerViewToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.viewListButton -> showListView()
                R.id.viewWeekButton -> showWeekView()
                R.id.viewMonthButton -> showMonthView()
            }
        }

        binding.generateTimetableButton.setOnClickListener { generateTimetable() }
        binding.addTaskFab.setOnClickListener { showAddTaskDialog() }

        selectedCalendarDay = startOfDay(System.currentTimeMillis())
        updateSelectedDateLabel()
        binding.monthlyCalendar.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, year)
                set(Calendar.MONTH, month)
                set(Calendar.DAY_OF_MONTH, dayOfMonth)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedCalendarDay = calendar.timeInMillis
            updateMonthlyTasks()
        }

        taskViewModel.allTasks.observe(viewLifecycleOwner) { tasks ->
            currentTasks = tasks
            adapter.submitList(tasks)
            binding.plannerEmptyState.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE

            if (generatedBlocks.isNotEmpty()) {
                generatedBlocks = SmartTimetableGenerator.generate(currentTasks)
                weeklyAdapter.submitBlocks(generatedBlocks)
                binding.timetableEmptyState.visibility = if (generatedBlocks.isEmpty()) View.VISIBLE else View.GONE
            }

            if (binding.plannerViewToggle.checkedButtonId == R.id.viewMonthButton) {
                updateMonthlyTasks()
            }
        }
    }

    private fun attachDragAndDrop() {
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN,
            0
        ) {
            override fun onMove(
                recyclerView: androidx.recyclerview.widget.RecyclerView,
                viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder,
                target: androidx.recyclerview.widget.RecyclerView.ViewHolder
            ): Boolean {
                weeklyAdapter.onItemMoved(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
                rebalanceSchedule()
                return true
            }

            override fun onSwiped(viewHolder: androidx.recyclerview.widget.RecyclerView.ViewHolder, direction: Int) {
                // no-op
            }
        }
        ItemTouchHelper(callback).attachToRecyclerView(binding.weeklyScheduleRecycler)
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

        dialogBinding.taskTypeToggle.check(dialogBinding.typeAssignmentButton.id)

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
                        val taskType = when (dialogBinding.taskTypeToggle.checkedButtonId) {
                            dialogBinding.typeExamButton.id -> TaskType.EXAM
                            dialogBinding.typeRevisionButton.id -> TaskType.REVISION
                            else -> TaskType.ASSIGNMENT
                        }
                        taskViewModel.addTask(title, subject, selectedDateMillis, duration, taskType)
                        addEventToGoogleCalendar(title, subject, selectedDateMillis, duration)
                        StudyNotificationScheduler.triggerImmediateRefresh(requireContext())
                        Snackbar.make(binding.root, R.string.planner_saved, Snackbar.LENGTH_SHORT)
                            .show()
                        generatedBlocks = emptyList()
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
                StudyNotificationScheduler.triggerImmediateRefresh(requireContext())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun updateDateLabel(binding: DialogAddTaskBinding, timeInMillis: Long) {
        val formatter = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        binding.selectedDate.text = formatter.format(timeInMillis)
    }

    private fun generateTimetable() {
        generatedBlocks = SmartTimetableGenerator.generate(currentTasks)
        weeklyAdapter.submitBlocks(generatedBlocks)
        val hasBlocks = generatedBlocks.isNotEmpty()
        binding.timetableEmptyState.visibility = if (hasBlocks) View.GONE else View.VISIBLE
        if (hasBlocks) {
            binding.plannerViewToggle.check(R.id.viewWeekButton)
            Snackbar.make(binding.root, R.string.planner_timetable_generated, Snackbar.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root, R.string.planner_timetable_empty_message, Snackbar.LENGTH_SHORT).show()
        }
        StudyNotificationScheduler.triggerImmediateRefresh(requireContext())
    }

    private fun showListView() {
        binding.tasksRecycler.visibility = View.VISIBLE
        binding.weeklyScheduleRecycler.visibility = View.GONE
        binding.monthlyContainer.visibility = View.GONE
        binding.timetableEmptyState.visibility = View.GONE
    }

    private fun showWeekView() {
        binding.tasksRecycler.visibility = View.GONE
        binding.weeklyScheduleRecycler.visibility = View.VISIBLE
        binding.monthlyContainer.visibility = View.GONE
        binding.timetableEmptyState.visibility = if (generatedBlocks.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showMonthView() {
        binding.tasksRecycler.visibility = View.GONE
        binding.weeklyScheduleRecycler.visibility = View.GONE
        binding.monthlyContainer.visibility = View.VISIBLE
        binding.timetableEmptyState.visibility = View.GONE
        updateMonthlyTasks()
    }

    private fun updateMonthlyTasks() {
        val dayStart = startOfDay(selectedCalendarDay)
        val dayEnd = dayStart + TimeUnit.DAYS.toMillis(1) - 1
        val tasksForDay = currentTasks.filter { it.dueDate in dayStart..dayEnd }
        monthlyAdapter.submitList(tasksForDay)
        binding.calendarEmptyState.visibility = if (tasksForDay.isEmpty()) View.VISIBLE else View.GONE
        updateSelectedDateLabel()
    }

    private fun updateSelectedDateLabel() {
        binding.calendarSelectedDate.text = dayFormatter.format(Date(selectedCalendarDay))
    }

    private fun rebalanceSchedule() {
        if (generatedBlocks.isEmpty()) return
        val ordered = weeklyAdapter.currentBlocks()
        if (ordered.isEmpty()) return

        val startCalendar = Calendar.getInstance().apply {
            timeInMillis = startOfDay(System.currentTimeMillis())
            set(Calendar.HOUR_OF_DAY, 16)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val recalculated = mutableListOf<StudyBlock>()
        var minutesUsed = 0
        var dayOffset = 0
        val dayCapacity = 5 * 60

        ordered.forEach { block ->
            if (minutesUsed + block.durationMinutes > dayCapacity) {
                dayOffset++
                minutesUsed = 0
            }

            val dayStart = (startCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            val startTime = dayStart.timeInMillis + TimeUnit.MINUTES.toMillis(minutesUsed.toLong())
            recalculated += block.copy(startTimeMillis = startTime)
            minutesUsed += block.durationMinutes
        }

        generatedBlocks = recalculated
        weeklyAdapter.submitBlocks(recalculated)
    }

    private fun startOfDay(timeInMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
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
                getString(R.string.planner_calendar_permission_prompt),
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
            Snackbar.make(
                binding.root,
                getString(R.string.planner_calendar_provider_missing),
                Snackbar.LENGTH_LONG
            ).show()
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
                    getString(R.string.planner_calendar_not_found),
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
                Snackbar.make(
                    binding.root,
                    getString(R.string.planner_calendar_added),
                    Snackbar.LENGTH_SHORT
                ).show()
                println("✅ Calendar event created at URI: $uri")
            } else {
                Snackbar.make(
                    binding.root,
                    getString(R.string.planner_calendar_failed),
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
    }
}
