package com.example.doctorquick.patient.model

import java.util.Date

data class Appointment(
    val bookingDate: Date? = null,
    val bookingDay: String = "",
    val bookingSlot: String = "",
    val checkupLocation: String = "",
    val doctorName: String = "",
    var appointmentId: String = ""  // To uniquely identify and delete the appointment
)
