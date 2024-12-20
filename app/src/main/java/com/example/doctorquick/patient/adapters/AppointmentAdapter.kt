package com.example.doctorquick.patient.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.databinding.ItemAllBookingBinding
import com.example.doctorquick.patient.model.Appointment
import java.text.SimpleDateFormat
import java.util.*

class AppointmentAdapter(
    private var appointments: List<Appointment>,
    private val onCancelClick: (Appointment) -> Unit
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    fun updateData(newAppointments: List<Appointment>) {
        appointments = newAppointments
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAllBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding, onCancelClick)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(appointments[position])
    }

    override fun getItemCount(): Int = appointments.size

    class ViewHolder(
        private val binding: ItemAllBookingBinding,
        private val onCancelClick: (Appointment) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(appointment: Appointment) {
            val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
            binding.tvBookingDate.text = "Booking Date: ${dateFormat.format(appointment.bookingDate)}"
            binding.tvBookingDay.text = "Booking Day: ${appointment.bookingDay}"
            binding.tvBookingTimeSlot.text = "Booking Slot Time: ${appointment.bookingSlot}"
            binding.tvCheckupLocation.text = "Checkup Location: ${appointment.checkupLocation}"
            binding.tvDoctorName.text = "Doctor Name: ${appointment.doctorName}"

            // Set today and appointment dates to midnight for accurate comparison
            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val appointmentDate = Calendar.getInstance().apply {
                time = appointment.bookingDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            // Enable or disable the cancel button based on the date comparison
            if (!appointmentDate.before(today)) {
                // Enable the button if the appointment is for today or a future date
                binding.btnCancelBooking.isEnabled = true
                binding.btnCancelBooking.setOnClickListener { onCancelClick(appointment) }
            } else {
                // Disable the button if the appointment is for a past date
                binding.btnCancelBooking.isEnabled = false
            }
        }
    }
}
