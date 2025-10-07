package com.example.studymate2.ui.planner

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.studymate2.data.StudyBlock
import com.example.studymate2.databinding.ItemWeeklyStudyBlockBinding
import com.example.studymate2.util.SubjectColorProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeeklyScheduleAdapter : RecyclerView.Adapter<WeeklyScheduleAdapter.BlockViewHolder>() {

    private val blocks = mutableListOf<StudyBlock>()
    private val dayFormatter = SimpleDateFormat("EEE, d MMM", Locale.getDefault())
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    fun submitBlocks(newBlocks: List<StudyBlock>) {
        blocks.clear()
        blocks.addAll(newBlocks.sortedBy { it.startTimeMillis })
        notifyDataSetChanged()
    }

    fun onItemMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) return
        val item = blocks.removeAt(fromPosition)
        blocks.add(if (toPosition > fromPosition) toPosition - 1 else toPosition, item)
        notifyItemMoved(fromPosition, toPosition)
    }

    fun currentBlocks(): List<StudyBlock> = blocks.toList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val binding = ItemWeeklyStudyBlockBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return BlockViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        holder.bind(blocks[position])
    }

    override fun getItemCount(): Int = blocks.size

    inner class BlockViewHolder(private val binding: ItemWeeklyStudyBlockBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(block: StudyBlock) {
            val context = binding.root.context
            binding.blockTitle.text = block.title
            binding.blockSubject.text = block.subject
            binding.blockTypeChip.setText(block.taskType.labelRes)
            binding.blockTypeChip.chipBackgroundColor =
                SubjectColorProvider.colorStateListForSubject(context, block.subject)
            binding.blockTypeChip.setTextColor(
                ContextCompat.getColor(context, android.R.color.white)
            )

            val start = Date(block.startTimeMillis)
            val end = Date(block.endTimeMillis)
            binding.blockSchedule.text = context.getString(
                com.example.studymate2.R.string.planner_block_time,
                dayFormatter.format(start),
                timeFormatter.format(start),
                timeFormatter.format(end)
            )
        }
    }
}
