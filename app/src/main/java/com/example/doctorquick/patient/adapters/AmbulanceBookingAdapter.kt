package com.example.doctorquick.patient.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.doctorquick.R
import com.example.doctorquick.patient.model.AmbulanceBooking
import com.google.firebase.firestore.FirebaseFirestore

class AmbulanceBookingAdapter(private val bookings: List<AmbulanceBooking>, private val firestore: FirebaseFirestore) :
    RecyclerView.Adapter<AmbulanceBookingAdapter.AmbulanceBookingViewHolder>() {

    inner class AmbulanceBookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ambulanceNoTextView: TextView = itemView.findViewById(R.id.tvAmbulanceNo)
        val bookingDateTimeTextView: TextView = itemView.findViewById(R.id.tvBookingDateTime)
        val startDestinationTextView: TextView = itemView.findViewById(R.id.tvStartDestination)
        val endDestinationTextView: TextView = itemView.findViewById(R.id.tvEndDestination)
        val bookingStatusTextView: TextView = itemView.findViewById(R.id.tvBookingStatus)
        val btnBookingCancelled: Button = itemView.findViewById(R.id.btnBookingCancelled)
        val btnBookingCompleted: Button = itemView.findViewById(R.id.btnBookingCompleted)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmbulanceBookingViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.item_all_booking_ambulance, parent, false
        )
        return AmbulanceBookingViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: AmbulanceBookingViewHolder, position: Int) {
        val currentBooking = bookings[position]

        holder.ambulanceNoTextView.text = "Ambulance No: ${currentBooking.ambulanceNo}"
        holder.bookingDateTimeTextView.text = "Booking Date & Time: ${currentBooking.bookingDate} ${currentBooking.bookingTime}"
        holder.startDestinationTextView.text = "Start Destination: ${currentBooking.startDestination}"
        holder.endDestinationTextView.text = "End Destination: ${currentBooking.endDestination}"
        holder.bookingStatusTextView.text = "Booking Status: ${currentBooking.bookingStatus}"

        // Disable buttons based on booking status
        holder.btnBookingCancelled.isEnabled = currentBooking.bookingStatus == "Booked"
        holder.btnBookingCompleted.isEnabled = currentBooking.bookingStatus == "Booked"

        holder.btnBookingCancelled.setOnClickListener {
            updateBookingStatus(currentBooking, "Cancelled")
            updateAmbulanceStatus(currentBooking.ambulanceNo, "Available")
        }

        holder.btnBookingCompleted.setOnClickListener {
            updateBookingStatus(currentBooking, "Completed")
            updateAmbulanceStatus(currentBooking.ambulanceNo, "Available")
        }
    }

    override fun getItemCount() = bookings.size

    private fun updateBookingStatus(booking: AmbulanceBooking, newStatus: String) {
        firestore.collection("ambulance_booking").document(booking.bookingId)
            .update("bookingStatus", newStatus)
            .addOnSuccessListener {
                // Optionally, update the booking in the bookings list and notify the adapter of the change
                booking.bookingStatus = newStatus
                notifyItemChanged(bookings.indexOf(booking))  // Update the specific item in the RecyclerView
                //Toast.makeText(holder.itemView.context, "Booking status updated to $newStatus", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { exception ->
                //Toast.makeText(holder.itemView.context, "Error updating booking status: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateAmbulanceStatus(ambulanceNo: String, newStatus: String) {
        firestore.collection("ambulance").document(ambulanceNo)
            .update("status", newStatus)
            .addOnSuccessListener { /* ... */ }
            .addOnFailureListener { exception ->
                //Toast.makeText(holder.itemView.context, "Error updating ambulance status: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
