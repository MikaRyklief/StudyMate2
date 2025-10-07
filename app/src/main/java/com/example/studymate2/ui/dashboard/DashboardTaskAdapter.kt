package com.example.studymate2.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate2.data.StudyTask
import com.example.studymate2.databinding.ItemDashboardTaskBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DashboardTaskAdapter : ListAdapter<StudyTask, DashboardTaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemDashboardTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(private val binding: ItemDashboardTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: StudyTask) {
            binding.dashboardTaskTitle.text = task.title
            binding.dashboardTaskSubject.text = task.subject
            binding.dashboardTaskDue.text = formatDueDate(task.dueDate, task.durationMinutes)
        }

        private fun formatDueDate(timestamp: Long, duration: Int): String {
            val date = Date(timestamp)
            val formattedDate = SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(date)
            return "$formattedDate • ${duration}m"
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StudyTask>() {
        override fun areItemsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem == newItem
    }
}
