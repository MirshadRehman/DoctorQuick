package com.example.doctorquick.patient

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.databinding.FragmentViewAllDoctorsBinding
import com.example.doctorquick.patient.adapters.DoctorAdapter
import com.example.doctorquick.patient.model.Doctor
import com.google.firebase.firestore.FirebaseFirestore

class ViewAllDoctors : Fragment() {

    private var _binding: FragmentViewAllDoctorsBinding? = null
    private val binding get() = _binding!!
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentViewAllDoctorsBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()

        setupRecyclerView()
        loadDoctors()
        setupSearchView()

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = DoctorAdapter(emptyList(), this::navigateToDoctorDetails)
    }

    private fun setupSearchView() {
        binding.svDoctor.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                (binding.recyclerView.adapter as DoctorAdapter).filter(newText ?: "")
                return true
            }
        })
    }

    private fun loadDoctors() {
        firestore.collection("doctors")
            .orderBy("doctorName")
            .get()
            .addOnSuccessListener { result ->
                val doctors = result.map { doc ->
                    Doctor(
                        doctorId = doc.id,
                        doctorName = doc.getString("doctorName") ?: "N/A",
                        qualification = doc.getString("qualification") ?: "N/A",
                        specialization = doc.getString("specialization") ?: "N/A",
                        consultancyFees = doc.getLong("consultancyFees") ?: 0,
                        doctorProfileImageUrl = doc.getString("doctorProfileImageUrl") ?: ""
                    )
                }
                (binding.recyclerView.adapter as DoctorAdapter).updateDoctors(doctors)
            }
            .addOnFailureListener { exception ->
                // Handle any errors
            }
    }

    private fun navigateToDoctorDetails(doctorId: String) {
        val action = ViewAllDoctorsDirections.actionViewAllDoctorsToViewIndividualDoctor(doctorId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
