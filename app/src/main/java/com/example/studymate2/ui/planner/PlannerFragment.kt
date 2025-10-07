package com.example.studymate2.ui.planner

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
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
import java.util.Calendar
import java.util.Locale

class PlannerFragment : Fragment() {

    private var _binding: FragmentPlannerBinding? = null
    private val binding get() = _binding!!

    private val taskViewModel: StudyTaskViewModel by activityViewModels {
        StudyTaskViewModelFactory(requireActivity().application)
    }

    private lateinit var adapter: StudyTaskAdapter

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
                    calendar.set(Calendar.HOUR_OF_DAY, 9)
                    calendar.set(Calendar.MINUTE, 0)
                    calendar.set(Calendar.SECOND, 0)
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
                        dialogBinding.inputTaskTitle.error = getString(R.string.planner_error_required)
                        dialogBinding.inputTaskSubject.error = getString(R.string.planner_error_required)
                    }
                    duration <= 0 -> {
                        dialogBinding.inputDuration.error = getString(R.string.planner_error_duration)
                    }
                    else -> {
                        taskViewModel.addTask(title, subject, selectedDateMillis, duration)
                        Snackbar.make(binding.root, R.string.planner_saved, Snackbar.LENGTH_SHORT).show()
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
}
