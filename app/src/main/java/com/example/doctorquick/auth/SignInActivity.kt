package com.example.doctorquick.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.doctorquick.MainActivity
import com.example.doctorquick.R
import com.example.doctorquick.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        setupSpinner()  // Setup the spinner with user types

        binding.btnSignIn.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val selectedUserType = binding.spUserType.selectedItem.toString()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signInUser(email, password, selectedUserType)
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }
        binding.tvSignUp.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun signInUser(email: String, password: String, selectedUserType: String) {
        binding.progressBar.visibility = View.VISIBLE  // Show the ProgressBar when the sign-in starts
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                checkUserType(email, selectedUserType)
            } else {
                binding.progressBar.visibility = View.GONE  // Hide the ProgressBar on failure
                Toast.makeText(this, "Sign In failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkUserType(email: String, selectedUserType: String) {
        firestore.collection("users").whereEqualTo("email", email).get().addOnSuccessListener { documents ->
            if (documents.isEmpty) {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "No such user exists", Toast.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            for (document in documents) {
                val userType = document.getString("userType")
                if (userType != null && userType == selectedUserType) {
                    navigateToMain(userType)
                    return@addOnSuccessListener
                }
            }
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "User type does not match", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            binding.progressBar.visibility = View.GONE
            Toast.makeText(this, "Failed to fetch user details: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain(userType: String) {
        binding.progressBar.visibility = View.GONE  // Hide the ProgressBar before navigation
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("userType", userType)
        }
        startActivity(intent)
        finish()
    }

    private fun setupSpinner() {
        // Assuming you have a string-array resource named user_types
        val adapter = ArrayAdapter.createFromResource(this, R.array.user_types, android.R.layout.simple_spinner_dropdown_item)
        binding.spUserType.adapter = adapter
    }
}
