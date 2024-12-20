package com.example.doctorquick.doctor

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
import com.example.doctorquick.databinding.FragmentProfileDoctorBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class ProfileDoctor : Fragment() {

    private var _binding: FragmentProfileDoctorBinding? = null
    private val binding get() = _binding!!

    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var imageUri: Uri? = null
    private var currentImageUrl: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileDoctorBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        storageReference = FirebaseStorage.getInstance().reference.child("doctors")

        val userId = arguments?.getString("userId")
        userId?.let {
            fetchDoctorDetails(it)
            fetchUserDetails(it)
        }

        binding.btnUploadImageDoctor.setOnClickListener {
            openFileChooser()
        }

        binding.btnSaveProfileDoctor.setOnClickListener {
            if (userId != null) {
                uploadImageThenSaveProfile(userId)
            }
        }

        return binding.root
    }

    private fun fetchUserDetails(userId: String) {
        firestore.collection("users").document(userId).get().addOnSuccessListener { document ->
            binding.etFullNameDoctor.setText(document.getString("userName"))
            binding.etEmail.setText(document.getString("email"))
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load user details: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchDoctorDetails(userId: String) {
        firestore.collection("doctors").document(userId).get().addOnSuccessListener { document ->
            document?.let {
                binding.etBio.setText(it.getString("bio"))
                binding.etUID.setText(it.getString("uid"))
                binding.etFullNameDoctor.setText(it.getString("doctorName"))
                binding.etQualification.setText(it.getString("qualification"))
                binding.etSpecialization.setText(it.getString("specialization"))
                binding.etExperience.setText(it.get("experience")?.toString())
                binding.etPhone.setText(it.getString("phone"))
                binding.etEmail.setText(it.getString("email"))
                binding.etHospitalName.setText(it.getString("hospitalName"))
                binding.etHospitalLocation.setText(it.getString("cityNameHospital"))
                binding.etClinicName.setText(it.getString("clinicName"))
                binding.etClinicLocation.setText(it.getString("cityNameClinic"))
                binding.etConsultancyFees.setText(it.get("consultancyFees")?.toString())

                // Load the profile image if it exists
                currentImageUrl = it.getString("doctorProfileImageUrl") // Update the current image URL
                if (!currentImageUrl.isNullOrEmpty()) {
                    Glide.with(this@ProfileDoctor)
                        .load(currentImageUrl)
                        .into(binding.ivProfileImage)
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Failed to load doctor details: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun openFileChooser() {
        val intent = Intent().apply {
            type = "image/*"
            action = Intent.ACTION_GET_CONTENT
        }
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            imageUri = data.data
            binding.ivProfileImage.setImageURI(imageUri)
        }
    }

    private fun uploadImageThenSaveProfile(userId: String) {
        if (imageUri != null) {
            binding.progressBar.visibility = View.VISIBLE
            val fileReference = storageReference.child("$userId.jpg")
            val uploadTask = fileReference.putFile(imageUri!!)
            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let {
                        throw it
                    }
                }
                fileReference.downloadUrl
            }.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUri = task.result
                    currentImageUrl = downloadUri.toString() // Update the current image URL on successful upload
                    saveDoctorProfile(currentImageUrl!!, userId)
                } else {
                    Toast.makeText(context, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
                binding.progressBar.visibility = View.GONE
            }
        } else {
            saveDoctorProfile(currentImageUrl ?: "", userId)  // Use the existing image URL if no new image is selected
        }
    }

    private fun saveDoctorProfile(imageUrl: String, userId: String) {
        val experienceValue = binding.etExperience.text.toString().trim()
        val consultancyFeesValue = binding.etConsultancyFees.text.toString().trim()

        val doctorDetails = hashMapOf(
            "doctorId" to userId,
            "doctorProfileImageUrl" to imageUrl,
            "bio" to binding.etBio.text.toString().trim(),
            "uid" to binding.etUID.text.toString().trim(),
            "doctorName" to binding.etFullNameDoctor.text.toString().trim(),
            "qualification" to binding.etQualification.text.toString().trim(),
            "specialization" to binding.etSpecialization.text.toString().trim(),
            "experience" to (if (experienceValue.isNotEmpty()) experienceValue.toInt() else 0),
            "phone" to binding.etPhone.text.toString().trim(),
            "email" to binding.etEmail.text.toString().trim(),
            "hospitalName" to binding.etHospitalName.text.toString().trim(),
            "cityNameHospital" to binding.etHospitalLocation.text.toString().trim(),
            "clinicName" to binding.etClinicName.text.toString().trim(),
            "cityNameClinic" to binding.etClinicLocation.text.toString().trim(),
            "consultancyFees" to (if (consultancyFeesValue.isNotEmpty()) consultancyFeesValue.toInt() else 0)
        )

        firestore.collection("doctors").document(userId).set(doctorDetails)
            .addOnSuccessListener {
                Toast.makeText(context, "Profile saved successfully", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error saving profile: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                binding.progressBar.visibility = View.GONE
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

