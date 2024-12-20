package com.example.doctorquick.doctor.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.R
import com.example.doctorquick.doctor.model.Appointment

import com.google.firebase.firestore.FirebaseFirestore

class AppointmentAdapter(
    private val appointments: List<Appointment>,
    private val firestore: FirebaseFirestore
) : RecyclerView.Adapter<AppointmentAdapter.AppointmentViewHolder>() {

    inner class AppointmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val bookingSlotTextView: TextView = itemView.findViewById(R.id.tvBookingSlot)
        val bookingDateTextView: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        val patientNameTextView: TextView = itemView.findViewById(R.id.tvPatientName)
        val checkupLocationTextView: TextView = itemView.findViewById(R.id.tvEndDestination)
        val bookingStatusTextView: TextView = itemView.findViewById(R.id.tvBookingStatus)
        val cancelButton: Button = itemView.findViewById(R.id.btnBookingCancelled)
        val completeButton: Button = itemView.findViewById(R.id.btnBookingCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppointmentViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_appointments, parent, false)
        return AppointmentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AppointmentViewHolder, position: Int) {
        val currentAppointment = appointments[position]

        holder.bookingSlotTextView.text = "Booking Slot: ${currentAppointment.bookingSlot}"
        holder.bookingDateTextView.text = "Booking Date: ${currentAppointment.bookingDate}"
        holder.patientNameTextView.text = "Patient Name: ${currentAppointment.patientName}"
        holder.checkupLocationTextView.text = "Location: ${currentAppointment.checkupLocation}"
        holder.bookingStatusTextView.text = "Booking Status: ${currentAppointment.bookingStatus}"

        // Disable buttons based on status
        holder.cancelButton.isEnabled = currentAppointment.bookingStatus == "Booked"
        holder.completeButton.isEnabled = currentAppointment.bookingStatus == "Booked"

        holder.cancelButton.setOnClickListener {
            updateBookingStatus(currentAppointment, "Cancelled", holder)
        }

        holder.completeButton.setOnClickListener {
            updateBookingStatus(currentAppointment, "Consultancy Completed", holder)
        }
    }

    override fun getItemCount() = appointments.size

    private fun updateBookingStatus(appointment: Appointment, newStatus: String, holder: AppointmentViewHolder) {
        firestore.collection("appointments").document(appointment.appointmentId)
            .update("bookingStatus", newStatus)
            .addOnSuccessListener {
                appointment.bookingStatus = newStatus
                notifyItemChanged(holder.adapterPosition) // Refresh the specific item
                Toast.makeText(holder.itemView.context, "Booking status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                Toast.makeText(holder.itemView.context, "Error updating status: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

