package com.example.doctorquick.patient.appointments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.databinding.FragmentDoctorAppointmentViewAllBinding
import com.example.doctorquick.patient.adapters.AppointmentAdapter
import com.example.doctorquick.patient.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class DoctorAppointmentViewAll : Fragment() {

    private var _binding: FragmentDoctorAppointmentViewAllBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDoctorAppointmentViewAllBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        setupRecyclerView()
        fetchAppointments()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = AppointmentAdapter(emptyList(), this::confirmCancelAppointment)
    }

    private fun fetchAppointments() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("appointments")
                .whereEqualTo("patientId", userId)
                .get()
                .addOnSuccessListener { result ->
                    val appointments = result.mapNotNull { doc ->
                        Appointment(
                            bookingDate = doc.getTimestamp("bookingDate")?.toDate(),
                            bookingDay = doc.getString("bookingDay")!!,
                            bookingSlot = doc.getString("bookingSlot")!!,
                            checkupLocation = doc.getString("checkupLocation")!!,
                            doctorName = doc.getString("doctorName")!!,
                            appointmentId = doc.id
                        )
                    }
                    if (appointments.isEmpty()) {
                        Toast.makeText(context, "No Appointments", Toast.LENGTH_SHORT).show()
                    } else {
                        (binding.recyclerView.adapter as AppointmentAdapter).updateData(appointments)
                    }
                }
                .addOnFailureListener { exception ->
                    Toast.makeText(context, "Error getting documents: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(context, "Not logged in", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmCancelAppointment(appointment: Appointment) {
        if (appointment.bookingDate?.before(Date()) == true) {
            Toast.makeText(context, "Cannot cancel today's appointment.", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(context)
            .setTitle("Cancel Appointment")
            .setMessage("Are you sure you want to cancel this appointment?")
            .setPositiveButton("Yes") { dialog, which ->
                cancelAppointment(appointment)
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelAppointment(appointment: Appointment) {
        firestore.collection("appointments").document(appointment.appointmentId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(context, "Appointment cancelled successfully", Toast.LENGTH_SHORT).show()
                fetchAppointments()  // Refresh the list after deletion
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error deleting document: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
