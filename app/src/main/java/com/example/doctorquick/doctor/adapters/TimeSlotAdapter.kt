package com.example.doctorquick.doctor.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.databinding.ItemTimeSlotAllBinding

class TimeSlotAdapter(private val onItemClicked: (String, String) -> Unit) :
    ListAdapter<TimeSlot, TimeSlotAdapter.TimeSlotViewHolder>(TimeSlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemTimeSlotAllBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeSlotViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TimeSlotViewHolder(private val binding: ItemTimeSlotAllBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(timeSlot: TimeSlot) {
            binding.apply {
                tvDayName.text = timeSlot.slotDay
                tvStartTime.text = timeSlot.slotStartTime
                tvEndTime.text = timeSlot.slotEndTime
                tvAvailableAt.text = timeSlot.availableAt
                tvSlotActive.text = if (timeSlot.isSlotActive) "Yes" else "No"
                root.setOnClickListener { onItemClicked(timeSlot.slotId, timeSlot.docId) } // Passing document ID as well
            }
        }
    }
}

class TimeSlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
    override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
        return oldItem.slotId == newItem.slotId
    }

    override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
        return oldItem == newItem
    }
}

data class TimeSlot(
    val docId: String,
    val slotId: String,
    val slotDay: String,
    val slotStartTime: String,
    val slotEndTime: String,
    val availableAt: String,
    val isSlotActive: Boolean,
    val slotStartTimeInMinutes: Int  // This is used for sorting
)
