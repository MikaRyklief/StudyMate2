package com.example.studymate2.ui.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate2.data.StudyTask
import com.example.studymate2.databinding.ItemCalendarTaskBinding
import com.example.studymate2.util.SubjectColorProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MonthlyTaskAdapter : ListAdapter<StudyTask, MonthlyTaskAdapter.TaskViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemCalendarTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TaskViewHolder(private val binding: ItemCalendarTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

        fun bind(task: StudyTask) {
            val context = binding.root.context
            binding.calendarTaskTitle.text = task.title
            binding.calendarTaskSubject.text = context.getString(
                com.example.studymate2.R.string.planner_calendar_subject,
                task.subject,
                context.getString(task.taskType.labelRes)
            )
            binding.calendarIndicator.backgroundTintList =
                SubjectColorProvider.colorStateListForSubject(context, task.subject)
            binding.calendarTaskTime.text = context.getString(
                com.example.studymate2.R.string.planner_calendar_due,
                timeFormatter.format(Date(task.dueDate))
            )
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StudyTask>() {
        override fun areItemsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudyTask, newItem: StudyTask): Boolean = oldItem == newItem
    }
}
