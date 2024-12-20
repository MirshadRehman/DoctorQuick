package com.example.doctorquick.patient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.doctorquick.R
import com.example.doctorquick.databinding.ItemAllDoctorsBinding
import com.example.doctorquick.patient.model.Doctor

class DoctorAdapter(
    private var doctors: List<Doctor>,
    private val onDetailsClick: (String) -> Unit
) : RecyclerView.Adapter<DoctorAdapter.DoctorViewHolder>() {

    private var filteredDoctors = doctors

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DoctorViewHolder {
        val binding = ItemAllDoctorsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DoctorViewHolder(binding, onDetailsClick)
    }

    override fun onBindViewHolder(holder: DoctorViewHolder, position: Int) {
        holder.bind(filteredDoctors[position])
    }

    override fun getItemCount() = filteredDoctors.size

    fun updateDoctors(newDoctors: List<Doctor>) {
        doctors = newDoctors
        filteredDoctors = newDoctors
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredDoctors = if (query.isEmpty()) {
            doctors
        } else {
            doctors.filter {
                it.doctorName.lowercase().contains(query.lowercase())
            }
        }
        notifyDataSetChanged()
    }

    class DoctorViewHolder(
        private val binding: ItemAllDoctorsBinding,
        private val onDetailsClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(doctor: Doctor) {
            binding.tvDoctorName.text = "Doctor Name: ${doctor.doctorName}"
            binding.tvQualification.text = "Qualification: ${doctor.qualification}"
            binding.tvSpecialization.text = "Specialization: ${doctor.specialization}"
            binding.tvConsultancyFees.text = "Consultancy Fees: ${doctor.consultancyFees}"

            Glide.with(binding.root.context)
                .load(doctor.doctorProfileImageUrl)
                .placeholder(R.drawable.doctor_placeholder)
                .into(binding.tvDoctorImage)

            binding.btnViewCompleteDetails.setOnClickListener {
                onDetailsClick(doctor.doctorId)
            }
        }
    }
}
