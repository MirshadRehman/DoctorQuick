package com.example.doctorquick.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.doctorquick.databinding.FragmentViewIndividualDoctorBinding
import com.google.firebase.firestore.FirebaseFirestore

class ViewIndividualDoctor : Fragment() {

    private var _binding: FragmentViewIndividualDoctorBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore
    private var doctorId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            doctorId = it.getString("doctorId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentViewIndividualDoctorBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        doctorId?.let {
            fetchDoctorDetails(it)
        }
        return binding.root
    }

    private fun fetchDoctorDetails(id: String) {
        firestore.collection("doctors").document(id)
            .get()
            .addOnSuccessListener { document ->
                // Load image using Glide
                val imageUrl = document.getString("doctorProfileImageUrl")
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(binding.ivDoctorImage)
                }

                binding.tvDoctorName.text = "Doctor Name: ${document.getString("doctorName")}"
                binding.tvQualification.text = "Qualification: ${document.getString("qualification")}"
                binding.tvSpecialization.text = "Specialization: ${document.getString("specialization")}"
                binding.tvConsultancyFees.text = "Consultancy Fees: ${document.getLong("consultancyFees")}"
                binding.tvDoctorRegistrationNo.text = "Doctor Registration No: ${document.getString("uid")}"
                binding.tvExperience.text = "Experience: ${document.getLong("experience")}"
                binding.tvEmail.text = "Email: ${document.getString("email")}"
                binding.tvPhone.text = "Phone: ${document.getString("phone")}"
                binding.tvHospitalName.text = "Hospital Name: ${document.getString("hospitalName")}"
                binding.tvClinicName.text = "Clinic Name: ${document.getString("clinicName")}"
                binding.tvBio.text = "Doctor Bio:\n${document.getString("bio")}"
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
