package com.example.doctorquick.doctor

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentDashboardAppointmentDoctorBinding

class DashboardAppointmentDoctor : Fragment() {

    private var _binding: FragmentDashboardAppointmentDoctorBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentDashboardAppointmentDoctorBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvCreateNewSlot.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAppointmentDoctor_to_timeSlotNew)
        }

        binding.tvViewAllSlots.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAppointmentDoctor_to_timeSlotAll)
        }

        binding.tvViewAllAppointments.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardAppointmentDoctor_to_viewAllAppointments)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}