package com.example.doctorquick.doctor

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.databinding.FragmentTimeSlotAllBinding
import com.example.doctorquick.doctor.adapters.TimeSlot
import com.example.doctorquick.doctor.adapters.TimeSlotAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot
import java.text.SimpleDateFormat
import java.util.*

class TimeSlotAll : Fragment() {

    private var _binding: FragmentTimeSlotAllBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: TimeSlotAdapter

    private val dayOrder = mapOf(
        "Sunday" to 1,
        "Monday" to 2,
        "Tuesday" to 3,
        "Wednesday" to 4,
        "Thursday" to 5,
        "Friday" to 6,
        "Saturday" to 7
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentTimeSlotAllBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        loadTimeSlots()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = TimeSlotAdapter { slotId, docId ->
            val action = TimeSlotAllDirections.actionTimeSlotAllToTimeSlotUpdate(slotId, docId)
            findNavController().navigate(action)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter
    }

    private fun loadTimeSlots() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("time_slots")
            .whereEqualTo("doctorId", userId)
            .get()
            .addOnSuccessListener { documents ->
                val slots = documents.mapNotNull { doc -> doc.toTimeSlot(doc.id) }
                    .sortedWith(compareBy({ dayOrder[it.slotDay] ?: Int.MAX_VALUE }, { it.slotStartTimeInMinutes }))
                adapter.submitList(slots)
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error loading time slots: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun QueryDocumentSnapshot.toTimeSlot(docId: String): TimeSlot? {
        return try {
            val startMinutes = getLong("slotStartTime") ?: 0
            val endMinutes = getLong("slotEndTime") ?: 0
            TimeSlot(
                docId = docId,
                slotId = getString("slotId") ?: return null,
                slotDay = getString("slotDay") ?: "",
                slotStartTime = minutesToTime(startMinutes),
                slotEndTime = minutesToTime(endMinutes),
                availableAt = getString("doctorAvailableAt") ?: "",
                isSlotActive = getBoolean("isSlotActive") ?: false,
                slotStartTimeInMinutes = startMinutes.toInt()  // Add this field to your TimeSlot data class
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun minutesToTime(minutes: Long): String {
        val hours = minutes / 60
        val mins = minutes % 60
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours.toInt())
            set(Calendar.MINUTE, mins.toInt())
        }
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(calendar.time)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
