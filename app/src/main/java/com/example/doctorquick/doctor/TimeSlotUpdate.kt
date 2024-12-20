package com.example.doctorquick.doctor


import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.doctorquick.databinding.FragmentTimeSlotUpdateBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotUpdate : Fragment() {

    private var _binding: FragmentTimeSlotUpdateBinding? = null
    private val binding get() = _binding!!
    private val args: TimeSlotUpdateArgs by navArgs()
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimeSlotUpdateBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        setupTimePickers()
        loadTimeSlotDetails()

        binding.btnUpdateTimeSlot.setOnClickListener { updateTimeSlot() }

        return binding.root
    }

    private fun setupTimePickers() {
        binding.dtpStartTime.setOnClickListener {
            showTimePicker(binding.dtpStartTime)
        }
        binding.dtpEndTime.setOnClickListener {
            showTimePicker(binding.dtpEndTime)
        }
    }

    private fun showTimePicker(textView: TextView) {
        val timeListener = TimePickerDialog.OnTimeSetListener { _, hourOfDay, minute ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            textView.text = timeFormat.format(calendar.time)
        }
        val currentTime = Calendar.getInstance()
        TimePickerDialog(context, timeListener, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show()
    }

    private fun loadTimeSlotDetails() {
        firestore.collection("time_slots").document(args.docId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    binding.spDays.setSelection((binding.spDays.adapter as ArrayAdapter<String>).getPosition(document.getString("slotDay")))
                    binding.dtpStartTime.text = minutesToTime(document.getLong("slotStartTime") ?: 0)
                    binding.dtpEndTime.text = minutesToTime(document.getLong("slotEndTime") ?: 0)
                    binding.spAvailableAt.setSelection((binding.spAvailableAt.adapter as ArrayAdapter<String>).getPosition(document.getString("availableAt")))
                    binding.cbIsActive.isChecked = document.getBoolean("isSlotActive") ?: false
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load time slot details: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun updateTimeSlot() {
        val updateData = mapOf(
            "slotDay" to (binding.spDays.selectedItem as String),
            "slotStartTime" to timeToMinutes(binding.dtpStartTime.text.toString()),
            "slotEndTime" to timeToMinutes(binding.dtpEndTime.text.toString()),
            "availableAt" to (binding.spAvailableAt.selectedItem as String),
            "isSlotActive" to binding.cbIsActive.isChecked
        )

        firestore.collection("time_slots").document(args.docId)
            .update(updateData)
            .addOnSuccessListener {
                Toast.makeText(context, "Time slot updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to update time slot: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun minutesToTime(minutes: Long): String {
        val hours = (minutes / 60).toInt()
        val mins = (minutes % 60).toInt()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, mins)
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    private fun timeToMinutes(time: String): Long {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val date = sdf.parse(time) ?: throw IllegalArgumentException("Invalid time format")
        val calendar = Calendar.getInstance().apply {
            setTime(date)  // Correct method to set the date on a Calendar instance
        }
        return (calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)).toLong()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
