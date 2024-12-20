package com.example.doctorquick.patient.adapters



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.databinding.ItemBookSlotBinding
import com.example.doctorquick.patient.model.TimeSlot

class TimeSlotAdapter(
    private val timeSlots: List<TimeSlot>,
    private val onBookClick: (TimeSlot) -> Unit
) : RecyclerView.Adapter<TimeSlotAdapter.TimeSlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        val binding = ItemBookSlotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TimeSlotViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        holder.bind(timeSlots[position])
    }

    override fun getItemCount(): Int = timeSlots.size

    class TimeSlotViewHolder(
        private val binding: ItemBookSlotBinding,
        private val onBookClick: (TimeSlot) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(timeSlot: TimeSlot) {
            binding.tvTimeSlot.text = "${timeSlot.startTime} - ${timeSlot.endTime}"
            binding.btnBookSlot.setOnClickListener { onBookClick(timeSlot) }
        }
    }
}
