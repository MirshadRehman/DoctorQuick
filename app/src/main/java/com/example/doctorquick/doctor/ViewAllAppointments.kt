package com.example.doctorquick.doctor

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentViewAllAppointmentsBinding
import com.example.doctorquick.doctor.adapters.AppointmentAdapter
import com.example.doctorquick.doctor.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class ViewAllAppointments : Fragment() {

    private var _binding: FragmentViewAllAppointmentsBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: AppointmentAdapter // Use a specific adapter for appointments
    private val appointmentsList = mutableListOf<Appointment>()
    private var selectedDate: String? = null // Store selected date in "dd-MM-yyyy" format


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentViewAllAppointmentsBinding.inflate(inflater, container, false)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        adapter = AppointmentAdapter(appointmentsList, firestore) // Corrected line
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = adapter

        setupDatePicker()
        binding.btnViewAppointment.setOnClickListener { fetchAppointmentsForDate() }

        return binding.root
    }

    private fun setupDatePicker() {
        binding.dtpAppointmentDate.setOnClickListener {
            val now = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                now.set(Calendar.YEAR, year)
                now.set(Calendar.MONTH, month)
                now.set(Calendar.DAY_OF_MONTH, day)
                val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                selectedDate = dateFormat.format(now.time) // Store formatted date
                binding.dtpAppointmentDate.text = selectedDate
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
    }

    private fun fetchAppointmentsForDate() {
        val doctorId = auth.currentUser?.uid ?: return

        firestore.collection("appointments")
            .whereEqualTo("doctorId", doctorId)
            .get()
            .addOnSuccessListener { documents ->
                appointmentsList.clear()
                for (document in documents) {
                    val timestamp = document.getTimestamp("bookingDate")
                    val bookingDate = if (timestamp != null) {
                        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(timestamp.toDate())
                    } else {
                        "" // Or a default value like "N/A"
                    }
                    if (bookingDate == selectedDate) {
                        val appointment = Appointment(
                            appointmentId = document.id,
                            bookingSlot = document.getString("bookingSlot") ?: "",
                            bookingDate = bookingDate,
                            patientName = document.getString("patientName") ?: "",
                            checkupLocation = document.getString("checkupLocation") ?: "",
                            bookingStatus = document.getString("bookingStatus") ?: "Booked" // Default if not set
                        )
                        appointmentsList.add(appointment)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Error fetching appointments: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}