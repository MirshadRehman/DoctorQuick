package com.example.doctorquick.patient.model

data class AmbulanceBooking(
    val bookingId: String = "",
    val bookingDate: String = "",
    val bookingTime: String = "",
    val ambulanceNo: String = "",
    var bookingStatus: String = "",
    val startDestination: String = "",
    val endDestination: String = "",
    val patientName: String = "",
    val patientId: String = ""
)

