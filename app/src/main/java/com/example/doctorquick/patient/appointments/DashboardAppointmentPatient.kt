package com.example.doctorquick.patient.appointments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentDashboardAppointmentPatientBinding


class DashboardAppointmentPatient : Fragment() {

    private var _binding: FragmentDashboardAppointmentPatientBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardAppointmentPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvNewAppointment.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAppointmentPatient_to_doctorAppointmentNew)
        }

        binding.tvViewAllAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAppointmentPatient_to_doctorAppointmentViewAll)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}