package com.example.doctorquick.patient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.databinding.ItemAllAmbulancesBinding
import com.example.doctorquick.patient.model.Ambulance

class AmbulanceAdapter(
    private var ambulances: List<Ambulance>,
    private val onBookClick: (Ambulance) -> Unit
) : RecyclerView.Adapter<AmbulanceAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllAmbulancesBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onBookClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(ambulances[position])
    }

    override fun getItemCount(): Int = ambulances.size

    fun updateAmbulances(newAmbulances: List<Ambulance>) {
        ambulances = newAmbulances
        notifyDataSetChanged()
    }

    class ViewHolder(
        private val binding: ItemAllAmbulancesBinding,
        private val onBookClick: (Ambulance) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ambulance: Ambulance) {
            binding.tvAmbulanceNo.text = "Ambulance No: ${ambulance.ambulanceNo}"
            binding.tvBookingStatus.text = "Booking Status: ${ambulance.status}"
            binding.btnBookNow.isEnabled = ambulance.status == "Available"

            binding.btnBookNow.setOnClickListener {
                if (ambulance.status == "Available") {
                    onBookClick(ambulance)
                }
            }
        }
    }
}
