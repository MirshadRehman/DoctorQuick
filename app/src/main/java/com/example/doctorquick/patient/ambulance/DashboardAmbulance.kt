package com.example.doctorquick.patient.ambulance

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentDashboardAmbulanceBinding
import com.example.doctorquick.patient.adapters.AmbulanceAdapter
import com.example.doctorquick.patient.model.Ambulance
import com.google.firebase.firestore.FirebaseFirestore

class DashboardAmbulance : Fragment() {

    private var _binding: FragmentDashboardAmbulanceBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentDashboardAmbulanceBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadAmbulances()

        binding.tvViewAllAmbulanceBookings.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAmbulance_to_viewAllAmbulanceBooking)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = AmbulanceAdapter(emptyList(), this::navigateToBookingNewAmbulance)
    }

    private fun loadAmbulances() {
        firestore.collection("ambulance")
            .get()
            .addOnSuccessListener { result ->
                val ambulances = result.map { doc ->
                    Ambulance(
                        ambulanceId = doc.id,
                        ambulanceNo = doc.getString("ambulanceNo") ?: "Unknown",
                        status = doc.getString("status") ?: "Unavailable"
                    )
                }
                if (binding.recyclerView.adapter is AmbulanceAdapter) {
                    (binding.recyclerView.adapter as AmbulanceAdapter).updateAmbulances(ambulances)
                }
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }
    }


    private fun navigateToBookingNewAmbulance(ambulance: Ambulance) {
        val action = DashboardAmbulanceDirections.actionDashboardAmbulanceToBookingNewAmbulance(ambulance.ambulanceNo)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
