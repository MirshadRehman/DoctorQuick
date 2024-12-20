package com.example.doctorquick.patient

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.doctorquick.databinding.FragmentDashboardPatientBinding
import com.example.doctorquick.patient.adapters.AppointmentAdapter
import com.example.doctorquick.patient.adapters.UpcomingAppointmentAdapter
import com.example.doctorquick.patient.model.Appointment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.*

class DashboardPatient : Fragment() {

    private var _binding: FragmentDashboardPatientBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDashboardPatientBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
