package com.example.studymate2.ui.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate2.data.StudyTask
import com.example.studymate2.databinding.ItemStudyTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            return "$due • ${task.durationMinutes} minutes"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StudyTask>() {
        override fun areItemsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem == newItem
    }
}
