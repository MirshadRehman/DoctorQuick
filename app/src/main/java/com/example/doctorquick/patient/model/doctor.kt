package com.example.doctorquick.patient.model

data class Doctor(
    val doctorId: String,
    val doctorName: String,
    val qualification: String,
    val specialization: String,
    val consultancyFees: Long,
    val doctorProfileImageUrl: String
)
