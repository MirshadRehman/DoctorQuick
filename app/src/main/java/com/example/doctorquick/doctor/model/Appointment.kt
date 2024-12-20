package com.example.doctorquick.doctor.model

data class Appointment(
    val appointmentId: String = "",   // Document ID from Firestore
    val bookingSlot: String = "",
    val bookingDate: String = "",  // Format: dd-MM-yyyy
    val patientName: String = "",
    val checkupLocation: String = "",
    var bookingStatus: String = "Booked" // Default status is "Booked"
)
