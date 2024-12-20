package com.example.doctorquick.patient

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.doctorquick.R
import com.example.doctorquick.databinding.FragmentProfilePatientBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class ProfilePatient : Fragment() {

    private var _binding: FragmentProfilePatientBinding? = null
    private val binding get() = _binding!!

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var profileImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfilePatientBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        loadInitialData()
        binding.btnUploadImage.setOnClickListener { openFileChooser() }
        binding.btnUpdatePatientProfile.setOnClickListener { savePatientDetails() }

        return binding.root
    }

    private fun loadInitialData() {
        val userId = auth.currentUser?.uid ?: return
        firestore.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                binding.etFullNamePatient.setText(document.getString("userName"))
                binding.etEmail.setText(document.getString("email"))
            }

        firestore.collection("patients").document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    Glide.with(this).load(document.getString("profileImageUrl")).into(binding.ivProfileImage)
                    binding.etMedicalHistory.setText(document.getString("patientMedicalHistory"))
                    binding.etFullNamePatient.setText(document.getString("patientName"))
                    binding.etAge.setText(document.getLong("age")?.toInt().toString())
                    binding.etPhone.setText(document.getString("phone"))
                    binding.etAddress.setText(document.getString("address"))
                    setGender(document.getString("gender"))
                }
            }
    }

    private fun setGender(gender: String?) {
        when (gender) {
            "Male" -> binding.rgGender.check(R.id.rbMale)
            "Female" -> binding.rgGender.check(R.id.rbFemale)
            "Others" -> binding.rgGender.check(R.id.rbOthers)
        }
    }

    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            profileImageUri = data.data
            Glide.with(this).load(profileImageUri).into(binding.ivProfileImage)
        }
    }

    private fun savePatientDetails() {
        binding.progressBar.visibility = View.VISIBLE
        val userId = auth.currentUser?.uid ?: return
        val patientName = binding.etFullNamePatient.text.toString()
        val patientMedicalHistory = binding.etMedicalHistory.text.toString()
        val gender = getSelectedGender()
        val age = binding.etAge.text.toString().toIntOrNull() ?: return
        val email = binding.etEmail.text.toString()
        val phone = binding.etPhone.text.toString()
        val address = binding.etAddress.text.toString()

        val patientDetails = hashMapOf(
            "patientId" to userId,
            "patientName" to patientName,
            "patientMedicalHistory" to patientMedicalHistory,
            "gender" to gender,
            "age" to age,
            "email" to email,
            "phone" to phone,
            "address" to address
        )

        if (profileImageUri != null) {
            val storageReference = FirebaseStorage.getInstance().getReference("patients/$userId.jpg")
            storageReference.putFile(profileImageUri!!)
                .continueWithTask { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    storageReference.downloadUrl
                }.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result.toString()
                        patientDetails["profileImageUrl"] = downloadUri
                        updateFirestore(userId, patientDetails)
                    } else {
                        Toast.makeText(context, "Image upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        binding.progressBar.visibility = View.GONE
                    }
                }
        } else {
            updateFirestore(userId, patientDetails)
        }
    }

    private fun updateFirestore(userId: String, patientDetails: Map<String, Any>) {
        firestore.collection("patients").document(userId).set(patientDetails)
            .addOnSuccessListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Failed to update details: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getSelectedGender(): String {
        return when (binding.rgGender.checkedRadioButtonId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            R.id.rbOthers -> "Others"
            else -> ""
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }
}
