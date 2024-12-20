package com.example.doctorquick.patient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.databinding.ItemUpcomingAppointmentsBinding
import com.example.doctorquick.patient.model.Appointment
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class UpcomingAppointmentAdapter(
    private var appointments: List<Appointment>,
) : RecyclerView.Adapter<UpcomingAppointmentAdapter.ViewHolder>() {

    fun updateData(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemUpcomingAppointmentsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    class ViewHolder(
        private val binding: ItemUpcomingAppointmentsBinding,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            binding.tvBookingDate.text = "Booking Date: ${dateFormat.format(appointment.bookingDate)}"
            binding.tvBookingDay.text = "Booking Day: ${appointment.bookingDay}"
            binding.tvBookingTimeSlot.text = "Booking Slot Time: ${appointment.bookingSlot}"
            binding.tvCheckupLocation.text = "Checkup Location: ${appointment.checkupLocation}"
            binding.tvDoctorName.text = "Doctor Name: ${appointment.doctorName}"

        }
    }
}
