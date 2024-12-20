package com.example.doctorquick.patient.ambulance

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentBookingNewAmbulanceBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class BookingNewAmbulance : Fragment() {

    private var _binding: FragmentBookingNewAmbulanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val args: BookingNewAmbulanceArgs by navArgs()
    private var bookingDate: String = ""
    private var bookingTime: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingNewAmbulanceBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.etAmbulanceNumber.setText(args.ambulanceNo)
        loadPatientName()
        setupDateTimePicker()

        binding.btnBookAmbulance.setOnClickListener { validateAndBook() }

        return binding.root
    }

    private fun loadPatientName() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("patients")
                .document(userId)
                .get()
                .addOnSuccessListener { document ->
                    val patientName = document.getString("patientName")
                    binding.etPatientName.setText(patientName)
                }
                .addOnFailureListener { e ->
                    // Log error or show a message to the user
                }
        }
    }

    private fun setupDateTimePicker() {
        binding.dtpBookingDateTime.setOnClickListener {
            val now = Calendar.getInstance()
            val datePicker = DatePickerDialog(requireContext(), { _, year, month, day ->
                val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                    now.set(year, month, day, hour, minute)

                    val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                    bookingDate = dateFormat.format(now.time)
                    bookingTime = timeFormat.format(now.time)

                    val format = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()) // Format with AM/PM
                    binding.dtpBookingDateTime.setText(format.format(now.time))
                }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false) // 'false' for 24-hour view
                timePicker.show()
            }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH))
            datePicker.show()
        }
    }


    private fun validateAndBook() {
        val patientName = binding.etPatientName.text.toString().trim()
        val bookingDateTime = binding.dtpBookingDateTime.text.toString().trim()
        val startDestination = binding.etStartDestination.text.toString().trim()
        val endDestination = binding.etEndDestination.text.toString().trim()
        val ambulanceNo = binding.etAmbulanceNumber.text.toString().trim()

        if (TextUtils.isEmpty(patientName) || TextUtils.isEmpty(bookingDateTime) || TextUtils.isEmpty(startDestination) ||
            TextUtils.isEmpty(endDestination)) {
            Toast.makeText(context, "All fields must be filled", Toast.LENGTH_SHORT).show()
            return
        }

        saveBooking(patientName, bookingDate.toString(), bookingTime, ambulanceNo, startDestination, endDestination)
    }

    private fun saveBooking(patientName: String, bookingDate: String, bookingTime: String, ambulanceNo: String, start: String, end: String) {
        val userId = auth.currentUser?.uid  // Get the current user's ID
        if (userId == null) {
            Toast.makeText(context, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingId = UUID.randomUUID().toString()
        val booking = hashMapOf(
            "bookingId" to bookingId,
            "bookingDate" to bookingDate,
            "bookingTime" to bookingTime,
            "ambulanceNo" to ambulanceNo,
            "bookingStatus" to "Booked",
            "startDestination" to start,
            "endDestination" to end,
            "patientName" to patientName,
            "patientId" to userId  // Include the patient's user ID
        )

        firestore.collection("ambulance_booking").document(bookingId).set(booking)
            .addOnSuccessListener {
                updateAmbulanceStatus(ambulanceNo)
                Toast.makeText(context, "Booking successful", Toast.LENGTH_SHORT).show()

                findNavController().navigate(R.id.action_bookingNewAmbulance_to_dashboardAmbulance) // Navigate back to DashboardAmbulance
            }
            .addOnFailureListener {
                Toast.makeText(context, "Booking failed: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAmbulanceStatus(ambulanceNo: String) {
        firestore.collection("ambulance").document(ambulanceNo)
            .update("status", "Booked")
            .addOnSuccessListener { }
            .addOnFailureListener { Toast.makeText(context, "Failed to update status: ${it.message}", Toast.LENGTH_SHORT).show() }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
