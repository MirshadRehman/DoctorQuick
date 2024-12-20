package com.example.doctorquick.patient.ambulance

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentViewAllAmbulanceBookingBinding
import com.example.doctorquick.patient.adapters.AmbulanceBookingAdapter
import com.example.doctorquick.patient.model.AmbulanceBooking
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class ViewAllAmbulanceBooking : Fragment() {

    private var _binding: FragmentViewAllAmbulanceBookingBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AmbulanceBookingAdapter
    private val bookingsList = mutableListOf<AmbulanceBooking>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewAllAmbulanceBookingBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = AmbulanceBookingAdapter(bookingsList, firestore)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        setupDatePicker()
        binding.btnViewBooking.setOnClickListener { fetchBookingsForDate() }


        return binding.root
    }

    private fun setupDatePicker() {
        binding.dtpBookingDate.setOnClickListener {
            val now = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                now.set(Calendar.YEAR, year)
                now.set(Calendar.MONTH, month)
                now.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                binding.dtpBookingDate.text = dateFormat.format(now.time)
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            datePicker.show()

        }

    }

    private fun fetchBookingsForDate() {
        val selectedDate = binding.dtpBookingDate.text.toString()
        val userId = auth.currentUser?.uid ?: return // Get current user ID or exit

        firestore.collection("ambulance_booking")
            .whereEqualTo("patientId", userId)
            .whereEqualTo("bookingDate", selectedDate) // Filter by date
            .get()
            .addOnSuccessListener { documents ->
                bookingsList.clear()
                for (document in documents) {
                    val booking = document.toObject(AmbulanceBooking::class.java)
                    bookingsList.add(booking)
                }
                adapter = AmbulanceBookingAdapter(bookingsList, firestore) // Pass firestore instance to adapter
                binding.recyclerView.adapter = adapter
                adapter.notifyDataSetChanged()

            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching bookings: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
