package com.example.studymate2.ui.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.core.content.ContextCompat
import com.example.studymate2.R
import com.example.studymate2.data.StudyTask
import com.example.studymate2.util.SubjectColorProvider
import com.example.studymate2.databinding.ItemStudyTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class StudyTaskAdapter(
    private val onToggleComplete: (StudyTask) -> Unit,
    private val onDelete: (StudyTask) -> Unit
) : ListAdapter<StudyTask, StudyTaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemStudyTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemStudyTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: StudyTask) {
            binding.taskTitle.text = task.title
            binding.taskSubject.text = task.subject
            binding.taskMeta.text = formatMeta(task)
            binding.taskTypeChip.setText(task.taskType.labelRes)
            binding.taskTypeChip.chipBackgroundColor =
                SubjectColorProvider.colorStateListForSubject(binding.root.context, task.subject)
            binding.taskTypeChip.setTextColor(
                ContextCompat.getColor(binding.root.context, android.R.color.white)
            )

            binding.taskCompleted.setOnCheckedChangeListener(null)
            binding.taskCompleted.isChecked = task.completed
            binding.taskCompleted.setOnCheckedChangeListener { _, isChecked ->
                if (task.completed != isChecked) {
                    onToggleComplete(task)
                }
            }

            binding.deleteButton.setOnClickListener { onDelete(task) }
        }

        private fun formatMeta(task: StudyTask): String {
            val formatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
            val due = formatter.format(Date(task.dueDate))
            val daysUntil = ((task.dueDate - System.currentTimeMillis()) / TimeUnit.DAYS.toMillis(1))
                .coerceAtLeast(0)
            return binding.root.context.getString(
                R.string.planner_task_meta,
                due,
                task.durationMinutes,
                daysUntil
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StudyTask>() {
        override fun areItemsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem == newItem
    }
}
