package com.example.doctorquick.patient.appointments

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.databinding.FragmentDoctorAppointmentNewBinding
import com.example.doctorquick.patient.adapters.TimeSlotAdapter
import com.example.doctorquick.patient.model.TimeSlot
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DoctorAppointmentNew : Fragment() {

    private var _binding: FragmentDoctorAppointmentNewBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val doctorNames = mutableListOf<String>()
    private val doctorIds = mutableListOf<String>()
    private var specialization: String? = null
    private var clinicName: String? = null
    private var hospitalName: String? = null
    private var bookingDate: Timestamp? = null
    private var bookingDay: String? = null
    private var selectedDoctorId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDoctorAppointmentNewBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fetchDoctors()
        setupDoctorSpinnerListener()
        setupBookingDatePicker()
        setupSearchButtonListener()


        return binding.root
    }



    private fun fetchDoctors() {
        firestore.collection("doctors")
            .get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    document.getString("doctorName")?.let {
                        doctorNames.add(it)
                        doctorIds.add(document.id)
                    }
                }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, doctorNames)
                binding.spDoctors.adapter = adapter
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun setupDoctorSpinnerListener() {
        binding.spDoctors.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedDoctorId = doctorIds[position]
                fetchDoctorDetails(selectedDoctorId!!)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun fetchDoctorDetails(doctorId: String) {
        firestore.collection("doctors").document(doctorId)
            .get()
            .addOnSuccessListener { document ->
                specialization = document.getString("specialization")
                clinicName = document.getString("clinicName")
                hospitalName = document.getString("hospitalName")

                binding.tvSpecialization.text = specialization

                val locations = mutableListOf<String>()
                clinicName?.let { locations.add("Clinic: $it") }
                hospitalName?.let { locations.add("Hospital: $it") }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, locations)
                binding.spCheckupLocation.adapter = adapter
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, "Failed to load doctor details: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setupBookingDatePicker() {
        binding.dtpBookingDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(requireContext(), { _, year, monthOfYear, dayOfMonth ->
                calendar.set(year, monthOfYear, dayOfMonth)
                val fmt = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                binding.dtpBookingDate.text = fmt.format(calendar.time)
                bookingDate = Timestamp(calendar.time)
                bookingDay = SimpleDateFormat("EEEE", Locale.getDefault()).format(calendar.time)  // Setting the day name
            }, year, month, day)

            dpd.datePicker.minDate = System.currentTimeMillis() - 1000 // Disallow past dates
            dpd.show()
        }
    }

    private fun setupSearchButtonListener() {
        binding.btnSearchTimeSlots.setOnClickListener {
            val selectedDoctorName = binding.spDoctors.selectedItem.toString()
            selectedDoctorId = doctorIds[doctorNames.indexOf(selectedDoctorName)]
            searchTimeSlots()
        }
    }

    private fun searchTimeSlots() {
        val doctorId = selectedDoctorId ?: return
        val locationFilter = binding.spCheckupLocation.selectedItem.toString().split(":").first().trim()

        firestore.collection("time_slots")
            .whereEqualTo("doctorId", doctorId)
            .whereEqualTo("slotDay", bookingDay)
            .whereEqualTo("isSlotActive", true)
            .get()
            .addOnSuccessListener { documents ->
                val timeSlots = documents.mapNotNull { doc ->
                    val availableAt = doc.getString("doctorAvailableAt") ?: ""
                    if (availableAt.equals(locationFilter, ignoreCase = true)) {
                        val forSlotSorting = doc.getLong("slotStartTime") ?: 0
                        val startTime = convertTime(doc.getLong("slotStartTime") ?: 0)
                        val endTime = convertTime(doc.getLong("slotEndTime") ?: 0)
                        TimeSlot(startTime, endTime, doc.id, forSlotSorting)
                    } else null
                }.sortedBy { it.forSlotSorting }
                if (timeSlots.isNotEmpty()) {
                    val adapter = TimeSlotAdapter(timeSlots) { timeSlot ->
                        bookAppointment(timeSlot)
                    }
                    binding.recyclerView.layoutManager = LinearLayoutManager(context)
                    binding.recyclerView.adapter = adapter
                } else {
                    binding.recyclerView.adapter = null // Clear RecyclerView if no slots found
                    Toast.makeText(context, "No time slots found for booking.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load time slots: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun bookAppointment(timeSlot: TimeSlot) {
        val patientId = auth.currentUser?.uid ?: return
        firestore.collection("patients").document(patientId)
            .get()
            .addOnSuccessListener { patientDoc ->
                val patientName = patientDoc.getString("patientName") ?: "Anonymous"
                val checkupLocation = if (binding.spCheckupLocation.selectedItem.toString().contains("Clinic")) clinicName else hospitalName
                val appointmentDetails = hashMapOf<String, Any?>(
                    "doctorId" to selectedDoctorId,
                    "doctorName" to binding.spDoctors.selectedItem.toString(),
                    "checkupLocation" to checkupLocation,
                    "bookingDay" to bookingDay,
                    "patientId" to patientId,
                    "patientName" to patientName,
                    "bookingSlot" to "${timeSlot.startTime} - ${timeSlot.endTime}",
                    "bookingDate" to bookingDate  // Ensure this is a Timestamp
                )

                // Check for existing appointment before booking
                checkForDuplicateAppointment(appointmentDetails) { exists ->
                    if (exists) {
                        Toast.makeText(context, "Appointment already booked for this slot.", Toast.LENGTH_SHORT).show()
                    } else {
                        createAppointment(appointmentDetails)
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to retrieve patient information: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkForDuplicateAppointment(details: HashMap<String, Any?>, callback: (Boolean) -> Unit) {
        firestore.collection("appointments")
            .whereEqualTo("doctorId", details["doctorId"])
            .whereEqualTo("checkupLocation", details["checkupLocation"])
            .whereEqualTo("bookingDay", details["bookingDay"])
            .whereEqualTo("patientId", details["patientId"])
            .whereEqualTo("bookingSlot", details["bookingSlot"])
            .get()
            .addOnSuccessListener { documents ->
                callback(documents.size() > 0)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to check for duplicate appointments: ${it.message}", Toast.LENGTH_SHORT).show()
                callback(false)
            }
    }

    private fun createAppointment(details: HashMap<String, Any?>) {
        firestore.collection("appointments").add(details)
            .addOnSuccessListener {
                Toast.makeText(context, "Appointment booked successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to book appointment: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }


    private fun convertTime(minutesPastMidnight: Long): String {
        val hours = minutesPastMidnight / 60
        val minutes = minutesPastMidnight % 60
        val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hours.toInt())
            set(Calendar.MINUTE, minutes.toInt())
        }
        return timeFormat.format(calendar.time)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


