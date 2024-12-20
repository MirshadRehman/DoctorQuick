package com.example.doctorquick.doctor

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.doctorquick.databinding.FragmentTimeSlotNewBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotNew : Fragment() {

    private var _binding: FragmentTimeSlotNewBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTimeSlotNewBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupTimePickers()
        binding.btnCreateTimeSlot.setOnClickListener { createTimeSlot() }

        return binding.root
    }

    private fun setupTimePickers() {
        binding.dtpStartTime.setOnClickListener {
            showTimePicker(true)
        }
        binding.dtpEndTime.setOnClickListener {
            showTimePicker(false)
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val formattedTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(calendar.time)
            val minutesAfterMidnight = hourOfDay * 60 + minute
            if (isStartTime) {
                binding.dtpStartTime.text = formattedTime
                binding.dtpStartTime.tag = minutesAfterMidnight
            } else {
                binding.dtpEndTime.text = formattedTime
                binding.dtpEndTime.tag = minutesAfterMidnight
            }
        }
        val currentTime = Calendar.getInstance()
        TimePickerDialog(context, timeListener,
            currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show()
    }

    private fun createTimeSlot() {
        val startTime = binding.dtpStartTime.tag as Int
        val endTime = binding.dtpEndTime.tag as Int
        val slotDay = binding.spDays.selectedItem.toString()
        val availableAt = binding.spAvailableAt.selectedItem.toString()
        val userId = auth.currentUser?.uid ?: return

        if (startTime >= endTime) {
            Toast.makeText(context, "End time must be after start time.", Toast.LENGTH_SHORT).show()
            return
        }

        val slotData = hashMapOf(
            "slotId" to UUID.randomUUID().toString(),
            "slotDay" to slotDay,
            "slotStartTime" to startTime,
            "slotEndTime" to endTime,
            "isSlotActive" to true,
            "doctorAvailableAt" to availableAt,
            "doctorId" to userId
        )

        // Creates an auto-generated document under the 'time_slots' collection
        firestore.collection("time_slots")
            .add(slotData)
            .addOnSuccessListener {
                Toast.makeText(context, "Time slot created successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to create time slot: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
