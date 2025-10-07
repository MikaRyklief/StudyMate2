package com.example.studymate2.ui.resources

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate2.R
import com.example.studymate2.databinding.ItemStudyResourceBinding
import com.example.studymate2.repository.StudyResource

class StudyResourceAdapter :
    ListAdapter<StudyResource, StudyResourceAdapter.ResourceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourceViewHolder {
        val binding = ItemStudyResourceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ResourceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ResourceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ResourceViewHolder(private val binding: ItemStudyResourceBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(resource: StudyResource) {
            binding.resourceTitle.text = resource.title
            binding.resourceDescription.text = resource.description
            val context = binding.root.context
            binding.resourceStatus.text = if (resource.completed) {
                context.getString(R.string.resources_completed)
            } else {
                context.getString(R.string.resources_suggested)
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<StudyResource>() {
        override fun areItemsTheSame(oldItem: StudyResource, newItem: StudyResource): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: StudyResource, newItem: StudyResource): Boolean = oldItem == newItem
    }
}
